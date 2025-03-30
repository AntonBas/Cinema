document.addEventListener('DOMContentLoaded', function() {
	const dropdownBtn = document.querySelector('.dropdown-btn');
	const dropdownMenu = document.querySelector('.dropdown-menu');

	dropdownBtn.addEventListener('click', function(e) {
		e.stopPropagation();
		dropdownMenu.classList.toggle('show');
	});

	document.addEventListener('click', function() {
		dropdownMenu.classList.remove('show');
	});

	dropdownMenu.addEventListener('click', function() {
		e.stopPropagation();
	});
});