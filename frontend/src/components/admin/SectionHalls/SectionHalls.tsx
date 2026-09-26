import React, { useState, useEffect, useCallback } from "react";
import type {
  CinemaHallListResponse,
  CinemaHallResponse,
  CinemaHallRequest,
} from "@/types/cinemaHall";
import { useCinemaHall } from "@/hooks/features/cinemaHall/useCinemaHall";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import { Button } from "@/components/ui/Button/Button";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { HallFormModal } from "./HallFormModal/HallFormModal";
import { HallsTable } from "./HallsTable/HallsTable";
import { HallLayoutModal } from "./HallLayoutModal/HallLayoutModal";
import { useHallLayout } from "./HallLayoutContext";
import { HallLayoutProvider } from "./HallLayoutProvider";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./SectionHalls.module.css";

const SectionHallsContent: React.FC = () => {
  const { loading, getAll, getById, create, update, remove } = useCinemaHall();
  const { openLayout, layoutSaveCount } = useHallLayout();

  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedHall, setSelectedHall] = useState<CinemaHallResponse | null>(
    null,
  );
  const [deleteModal, setDeleteModal] = useState({
    isOpen: false,
    hall: null as CinemaHallListResponse | null,
    isDeleting: false,
  });
  const [hallsData, setHallsData] = useState<CinemaHallListResponse[]>([]);

  const loadHalls = useCallback(async () => {
    const response = await getAll();
    setHallsData(response || []);
  }, [getAll]);

  useEffect(() => {
    loadHalls().catch(() => {});
  }, [loadHalls, layoutSaveCount]);

  const handleCreateHall = useCallback(
    async (request: CinemaHallRequest) => {
      const response = await create(request);
      if (response) {
        await loadHalls();
        setShowCreateModal(false);
        openLayout(response);
      }
    },
    [create, loadHalls, openLayout],
  );

  const handleEditHall = useCallback(
    async (id: number, request: CinemaHallRequest) => {
      const response = await update(id, request);
      if (response) {
        await loadHalls();
        setShowEditModal(false);
        setSelectedHall(null);
      }
    },
    [update, loadHalls],
  );

  const handleDeleteHall = useCallback(async () => {
    if (!deleteModal.hall) return;

    setDeleteModal((prev) => ({ ...prev, isDeleting: true }));

    try {
      await remove(deleteModal.hall.id);
      await loadHalls();
      setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
    } catch {
      setDeleteModal((prev) => ({ ...prev, isDeleting: false }));
    }
  }, [deleteModal.hall, remove, loadHalls]);

  const confirmDelete = useCallback((hall: CinemaHallListResponse) => {
    setDeleteModal({ isOpen: true, hall, isDeleting: false });
  }, []);

  const handleEdit = useCallback(
    async (hall: CinemaHallListResponse) => {
      try {
        const response = await getById(hall.id);
        if (response) {
          setSelectedHall(response);
          setShowEditModal(true);
        }
      } catch {
        return;
      }
    },
    [getById],
  );

  const handleShowLayout = useCallback(
    async (hall: CinemaHallListResponse) => {
      try {
        const response = await getById(hall.id);
        if (response) {
          openLayout(response);
        }
      } catch {
        return;
      }
    },
    [getById, openLayout],
  );

  if (showDelayedLoading && !hallsData.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading cinema halls..." />
      </div>
    );
  }

  return (
    <div className={styles.section}>
      <PageHeader
        title="Halls"
        subtitle="Manage your cinema halls, seating layouts and configurations"
        actions={
          <Button
            variant="primary"
            onClick={() => setShowCreateModal(true)}
            disabled={loading}
          >
            Add Hall
          </Button>
        }
      />

      <div className={styles.content}>
        <HallsTable
          halls={hallsData}
          onDelete={confirmDelete}
          onShowLayout={handleShowLayout}
          onEdit={handleEdit}
        />
      </div>

      {showCreateModal && (
        <HallFormModal
          onClose={() => setShowCreateModal(false)}
          onSave={handleCreateHall}
          loading={loading}
        />
      )}

      {showEditModal && selectedHall && (
        <HallFormModal
          hall={selectedHall}
          onClose={() => {
            setShowEditModal(false);
            setSelectedHall(null);
          }}
          onSave={(request) => handleEditHall(selectedHall.id, request)}
          loading={loading}
        />
      )}

      <HallLayoutModal />

      <DeleteConfirmModal
        isOpen={deleteModal.isOpen}
        onConfirm={handleDeleteHall}
        onCancel={() =>
          setDeleteModal({ isOpen: false, hall: null, isDeleting: false })
        }
        itemName={deleteModal.hall?.name}
        itemType="cinema hall"
        isDeleting={deleteModal.isDeleting}
      />
    </div>
  );
};

export const SectionHalls: React.FC = () => {
  return (
    <HallLayoutProvider>
      <SectionHallsContent />
    </HallLayoutProvider>
  );
};
