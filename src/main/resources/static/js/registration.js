document.addEventListener('DOMContentLoaded', function() {
  const dateInput = document.getElementById('dateOfBirth');
  const datePlaceholder = document.getElementById('datePlaceholder');

  datePlaceholder.addEventListener('click', function() {
    dateInput.focus();
  });

  dateInput.addEventListener('blur', function() {
    if (!this.value) {
      datePlaceholder.style.opacity = '1';
    }
  });

  dateInput.addEventListener('focus', function() {
    datePlaceholder.style.opacity = '0';
  });
});