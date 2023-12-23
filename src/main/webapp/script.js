let progress = 0;

document.addEventListener("DOMContentLoaded", function () {
    const captureButton = document.getElementById("submitButton");

    captureButton.addEventListener("click", function () {
        const homePageURL = document.getElementById("url").value;
        const fileInput = document.getElementById("fileInput");
        if(fileInput.value === ''){
			document.getElementById('progressBar').innerHTML = "Upload the file.";
		} else {
       /* const itemCodeFilePath = document.getElementById("itemCodeFilePath").value;*/

        const file = fileInput.files[0];
        const formData = new FormData();

        formData.append("homePageUrl", homePageURL);
      /*  formData.append("itemCodeFilePath", itemCodeFilePath);*/
        formData.append("file", file);

        // Use window.location to get the base URL
		const baseUrl = window.location.href;

		//fetch(`${baseUrl.substring(0, baseUrl.length-1)}/processExcel`, {
		fetch(`${baseUrl}processExcel`, {
            method: 'POST',
            enctype: 'multipart/form-data',
            body: formData,
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(responseText => {
            console.log(responseText);
            document.getElementById('progressBar').innerHTML = responseText;
            // Call simulateProgress after the server-side processing is complete
          //  simulateProgress();
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
        }
    });
});

function simulateProgress() {
    const progressBar = document.getElementById('progressBar');
    progressBar.style.width = progress + '%';
    
    // Simulate image download progress (update 'progress' accordingly)
    if (progress < 100) {
        progress += 10;
        setTimeout(simulateProgress, 500);
    }
}
