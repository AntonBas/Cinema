document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab-title');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            document.querySelectorAll('.tab-title').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            
            this.classList.add('active');
			
            const tabId = this.getAttribute('data-tab');
            document.getElementById(tabId).classList.add('active');
        });
    });
});