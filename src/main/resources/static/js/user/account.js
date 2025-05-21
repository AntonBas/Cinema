document.addEventListener('DOMContentLoaded', function() {
           const editProfileBtn = document.getElementById('editProfileBtn');
           const cancelEditBtn = document.getElementById('cancelEditBtn');
           const profileInfo = document.getElementById('profileInfo');
           const editProfileForm = document.getElementById('editProfileForm');
           
           editProfileBtn.addEventListener('click', function() {
               profileInfo.classList.remove('active-info');
               profileInfo.classList.add('hidden');
               editProfileForm.classList.add('active-form');
               editProfileBtn.classList.add('hidden');
           });
           
           cancelEditBtn.addEventListener('click', function() {
               profileInfo.classList.add('active-info');
               profileInfo.classList.remove('hidden');
               editProfileForm.classList.remove('active-form');
               editProfileBtn.classList.remove('hidden');
           });
           
           const tabBtns = document.querySelectorAll('.tab-btn');
           tabBtns.forEach(btn => {
               btn.addEventListener('click', function() {
                   tabBtns.forEach(b => b.classList.remove('active'));
                   document.querySelectorAll('.tab-content').forEach(content => {
                       content.classList.remove('active');
                   });
                   
                   this.classList.add('active');
                   const tabId = this.getAttribute('data-tab');
                   document.getElementById(tabId).classList.add('active');
               });
           });
       });