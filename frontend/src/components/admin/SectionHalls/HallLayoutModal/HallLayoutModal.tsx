import React, { useMemo, useRef, useState } from "react";
import { SeatType } from "@/types/seat";
import { Modal } from "@/components/ui/Modal/Modal";
import { Button } from "@/components/ui/Button/Button";
import { ConfirmModal } from "@/components/ui/ConfirmModal/ConfirmModal";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { useHallLayout, type DraftSeat } from "../HallLayoutContext";
import {
  GRID_COLS,
  GRID_ROWS,
  CELL_WIDTH,
  CELL_HEIGHT,
} from "@/utils/hallLayoutGrid";
import styles from "./HallLayoutModal.module.css";

const getSeatTypeName = (seatType: SeatType): string => {
  const names: Record<SeatType, string> = {
    [SeatType.STANDARD]: "Standard",
    [SeatType.VIP]: "VIP",
    [SeatType.COUPLE]: "Couple",
  };
  return names[seatType];
};

interface SeatTileProps {
  seat: DraftSeat;
  number: number;
  onCycleType: () => void;
  onToggleActive: () => void;
  onRemove: () => void;
  onDragStart: () => void;
}

const SeatTile: React.FC<SeatTileProps> = ({
  seat,
  number,
  onCycleType,
  onToggleActive,
  onRemove,
  onDragStart,
}) => {
  const width =
    seat.seatType === SeatType.COUPLE ? CELL_WIDTH * 2 - 8 : CELL_WIDTH - 8;

  return (
    <div
      className={`${styles.seatTile} ${styles[seat.seatType.toLowerCase()]} ${!seat.active ? styles.inactive : ""}`}
      style={{
        left: seat.col * CELL_WIDTH + 4,
        top: seat.gridRow * CELL_HEIGHT + 4,
        width,
        height: CELL_HEIGHT - 8,
      }}
      draggable
      onDragStart={(e) => {
        e.dataTransfer.setData("text/plain", seat.key);
        onDragStart();
      }}
      onClick={onCycleType}
      onContextMenu={(e) => {
        e.preventDefault();
        onToggleActive();
      }}
      title={`Row ${seat.gridRow + 1}, Seat ${number}\nType: ${getSeatTypeName(seat.seatType)}\nStatus: ${seat.active ? "Active" : "Inactive"}\n\nClick: change type\nRight click: toggle status\nDrag: move\n×: delete`}
    >
      <span className={styles.seatNumber}>
        {seat.gridRow + 1}-{number}
      </span>
      <button
        type="button"
        className={styles.deleteButton}
        onClick={(e) => {
          e.stopPropagation();
          onRemove();
        }}
        aria-label="Remove seat"
      >
        ×
      </button>
      {!seat.active && <div className={styles.inactiveOverlay} />}
    </div>
  );
};

export const HallLayoutModal: React.FC = () => {
  const {
    currentHall,
    layout,
    seats,
    isDirty,
    saving,
    loading,
    closeLayout,
    addSeat,
    moveSeat,
    removeSeat,
    cycleSeatType,
    toggleSeatActive,
    saveLayout,
  } = useHallLayout();

  const showLoading = useDelayedLoading(loading);
  const draggedKeyRef = useRef<string | null>(null);
  const [showDiscardConfirm, setShowDiscardConfirm] = useState(false);

  const occupiedCells = useMemo(() => {
    const cells = new Set<string>();
    seats.forEach((seat) => {
      cells.add(`${seat.col}:${seat.gridRow}`);
      if (seat.seatType === SeatType.COUPLE) {
        cells.add(`${seat.col + 1}:${seat.gridRow}`);
      }
    });
    return cells;
  }, [seats]);

  const activeSeatsCount = useMemo(
    () => seats.filter((s) => s.active).length,
    [seats],
  );

  const numberByKey = useMemo(() => {
    const byRow = new Map<number, DraftSeat[]>();
    seats.forEach((seat) => {
      const rowSeats = byRow.get(seat.gridRow) ?? [];
      rowSeats.push(seat);
      byRow.set(seat.gridRow, rowSeats);
    });
    const result = new Map<string, number>();
    byRow.forEach((rowSeats) => {
      [...rowSeats]
        .sort((a, b) => a.col - b.col)
        .forEach((seat, index) => {
          result.set(seat.key, index + 1);
        });
    });
    return result;
  }, [seats]);

  if (!currentHall) return null;

  const handleClose = () => {
    if (isDirty) {
      setShowDiscardConfirm(true);
      return;
    }
    closeLayout();
  };

  const handleConfirmDiscard = () => {
    setShowDiscardConfirm(false);
    closeLayout();
  };

  const handleSave = async () => {
    await saveLayout();
  };

  const emptyCells: { col: number; gridRow: number }[] = [];
  for (let gridRow = 0; gridRow < GRID_ROWS; gridRow++) {
    for (let col = 0; col < GRID_COLS; col++) {
      if (!occupiedCells.has(`${col}:${gridRow}`)) {
        emptyCells.push({ col, gridRow });
      }
    }
  }

  return (
    <Modal
      isOpen={!!currentHall}
      onClose={handleClose}
      title={`${currentHall.name} - Seat Layout`}
      size="fullscreen"
    >
      <div className={styles.modalContent}>
        {showLoading && !layout && (
          <div className={styles.loading}>
            <LoadingSpinner text="Loading hall layout..." />
          </div>
        )}

        {layout && (
          <div className={styles.cinemaHall}>
            <div className={styles.headerControls}>
              <div className={styles.stats}>
                <div className={styles.statItem}>
                  <span className={styles.statNumber}>{seats.length}</span>
                  <span className={styles.statLabel}>Total Seats</span>
                </div>
                <div className={styles.statItem}>
                  <span className={styles.statNumber}>{activeSeatsCount}</span>
                  <span className={styles.statLabel}>Active</span>
                </div>
                <div className={styles.statItem}>
                  <span className={styles.statNumber}>
                    {seats.length - activeSeatsCount}
                  </span>
                  <span className={styles.statLabel}>Inactive</span>
                </div>
              </div>
            </div>

            <div className={styles.screenArea}>
              <div className={styles.screen}>SCREEN</div>
              <div className={styles.screenReflection} />
            </div>

            <div className={styles.gridWrapper}>
              <div
                className={styles.canvas}
                style={{
                  width: GRID_COLS * CELL_WIDTH,
                  height: GRID_ROWS * CELL_HEIGHT,
                }}
              >
                {emptyCells.map(({ col, gridRow }) => (
                  <div
                    key={`empty-${col}-${gridRow}`}
                    className={styles.gridCell}
                    style={{
                      left: col * CELL_WIDTH,
                      top: gridRow * CELL_HEIGHT,
                      width: CELL_WIDTH,
                      height: CELL_HEIGHT,
                    }}
                    onClick={() => addSeat(col, gridRow)}
                    onDragOver={(e) => e.preventDefault()}
                    onDrop={(e) => {
                      e.preventDefault();
                      const key =
                        e.dataTransfer.getData("text/plain") ||
                        draggedKeyRef.current;
                      if (key) moveSeat(key, col, gridRow);
                    }}
                  />
                ))}
                {seats.map((seat) => (
                  <SeatTile
                    key={seat.key}
                    seat={seat}
                    number={numberByKey.get(seat.key) ?? 0}
                    onCycleType={() => cycleSeatType(seat.key)}
                    onToggleActive={() => toggleSeatActive(seat.key)}
                    onRemove={() => removeSeat(seat.key)}
                    onDragStart={() => {
                      draggedKeyRef.current = seat.key;
                    }}
                  />
                ))}
              </div>
            </div>

            <div className={styles.footer}>
              <div className={styles.legend}>
                <h4 className={styles.legendTitle}>Seat Types:</h4>
                <div className={styles.legendGrid}>
                  {Object.values(SeatType).map((type) => (
                    <div key={type} className={styles.legendItem}>
                      <div
                        className={`${styles.legendColor} ${styles[type.toLowerCase()]}`}
                      />
                      <span>{getSeatTypeName(type)}</span>
                    </div>
                  ))}
                  <div className={styles.legendItem}>
                    <div
                      className={`${styles.legendColor} ${styles.inactive}`}
                    />
                    <span>Inactive</span>
                  </div>
                </div>
              </div>

              <div className={styles.instructions}>
                <div className={styles.instructionIcon}>🎯</div>
                <div className={styles.instructionText}>
                  <p>
                    <strong>Click empty cell:</strong> add a seat
                  </p>
                  <p>
                    <strong>Click seat:</strong> change type
                  </p>
                  <p>
                    <strong>Right click seat:</strong> toggle active
                  </p>
                  <p>
                    <strong>Drag seat:</strong> reposition
                  </p>
                  <p className={styles.note}>
                    Row/seat numbers are assigned automatically left-to-right
                    per row
                  </p>
                </div>
              </div>

              <div className={styles.actions}>
                <Button
                  variant="cancel"
                  onClick={handleClose}
                  disabled={saving}
                >
                  Close
                </Button>
                <Button
                  variant="primary"
                  onClick={handleSave}
                  disabled={!isDirty || saving}
                  loading={saving}
                >
                  Save Layout
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={showDiscardConfirm}
        onConfirm={handleConfirmDiscard}
        onCancel={() => setShowDiscardConfirm(false)}
        title="Discard Changes?"
        message="You have unsaved layout changes. Are you sure you want to discard them?"
        confirmText="Discard"
        cancelText="Keep editing"
        variant="error"
      />
    </Modal>
  );
};
