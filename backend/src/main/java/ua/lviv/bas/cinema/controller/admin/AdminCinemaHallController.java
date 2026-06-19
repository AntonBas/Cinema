package ua.lviv.bas.cinema.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.service.cinema.CinemaHallService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/cinema-halls")
@RequiredArgsConstructor
@Tag(name = "Admin Cinema Hall Management", description = "Admin endpoints for managing cinema halls")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminCinemaHallController {

    private final CinemaHallService cinemaHallService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new cinema hall")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cinema hall created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Hall with this name already exists")
    })
    public CinemaHallResponse createHall(@Valid @RequestBody CinemaHallRequest request) {
        log.info("POST /api/admin/cinema-halls - Creating new cinema hall: {}", request.name());
        return cinemaHallService.createHall(request);
    }

    @GetMapping
    @Operation(summary = "Get all cinema halls")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Halls retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public List<CinemaHallListResponse> getHalls() {
        log.info("GET /api/admin/cinema-halls - Getting all cinema halls");
        return cinemaHallService.getHalls();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cinema hall by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hall found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Hall not found")
    })
    public CinemaHallResponse getHall(@PathVariable Long id) {
        log.info("GET /api/admin/cinema-halls/{} - Getting cinema hall", id);
        return cinemaHallService.getHall(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update cinema hall")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hall updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Hall not found"),
            @ApiResponse(responseCode = "409", description = "Cannot update hall with active sessions")
    })
    public CinemaHallResponse updateHall(@PathVariable Long id, @Valid @RequestBody CinemaHallRequest request) {
        log.info("PUT /api/admin/cinema-halls/{} - Updating cinema hall", id);
        return cinemaHallService.updateHall(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete cinema hall")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hall deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Hall not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete hall with active sessions")
    })
    public void deleteHall(@PathVariable Long id) {
        log.info("DELETE /api/admin/cinema-halls/{} - Deleting cinema hall", id);
        cinemaHallService.deleteHall(id);
    }

    @GetMapping("/{id}/layout")
    @Operation(summary = "Get hall layout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Layout retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Hall not found")
    })
    public HallLayoutResponse getHallLayout(@PathVariable Long id) {
        log.info("GET /api/admin/cinema-halls/{}/layout - Getting hall layout", id);
        return cinemaHallService.getHallLayout(id);
    }
}