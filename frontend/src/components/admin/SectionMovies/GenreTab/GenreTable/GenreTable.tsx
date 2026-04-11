import React from 'react';
import type { GenreListResponse } from '@/types/genre';
import { Button, Badge } from '@/components/ui';
import styles from './GenreTable.module.css';

interface GenreTableProps {
    genres: GenreListResponse[];
    onEdit: (genre: GenreListResponse) => void;
    onDelete: (genre: GenreListResponse) => void;
}

const getMovieCountText = (count: number): string =>
    `${count} ${count === 1 ? 'movie' : 'movies'}`;

export const GenreTable: React.FC<GenreTableProps> = React.memo(({
    genres,
    onEdit,
    onDelete
}) => {
    if (genres.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>📚</div>
                <h3>No genres found</h3>
                <p>Create your first genre to get started!</p>
            </div>
        );
    }

    return (
        <table className={styles.table}>
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Movies</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                {genres.map((genre) => (
                    <tr key={genre.id}>
                        <td>
                            <span className={styles.name}>{genre.name}</span>
                        </td>
                        <td>
                            <Badge variant="primary">
                                {getMovieCountText(genre.movieCount)}
                            </Badge>
                        </td>
                        <td>
                            <div className={styles.actions}>
                                <Button
                                    variant="success"
                                    size="small"
                                    onClick={() => onEdit(genre)}
                                >
                                    Edit
                                </Button>
                                <Button
                                    variant="error"
                                    size="small"
                                    onClick={() => onDelete(genre)}
                                >
                                    Delete
                                </Button>
                            </div>
                        </td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
});

GenreTable.displayName = 'GenreTable';