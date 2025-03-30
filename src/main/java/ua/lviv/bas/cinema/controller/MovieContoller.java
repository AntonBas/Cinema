package ua.lviv.bas.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.service.MovieService;

@Controller
public class MovieContoller {

	@Autowired
	MovieService movieService;

	@RequestMapping(value = "/create-movie", method = RequestMethod.GET)
	public ModelAndView createMovie() {
		return new ModelAndView("createMovie", "movie", new Movie());
	}

	public String createMovie(@Valid @ModelAttribute("movie") Movie movie, BindingResult bindingResult) {
		movieService.save(movie);
		return "redirect:/home";
	}

	public ModelAndView showMovies() {
		ModelAndView map = new ModelAndView("home");
		map.addObject("movies", movieService.getAllMovies());
		return map;
	}
}
