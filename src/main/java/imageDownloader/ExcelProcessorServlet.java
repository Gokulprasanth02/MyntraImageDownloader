package imageDownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

@SuppressWarnings("serial")
@MultipartConfig(maxFileSize = 1024 * 1024 * 5) // Set to 5 MB, adjust as needed
@WebServlet("/processExcel")
public class ExcelProcessorServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setCharacterEncoding("UTF-8");

        try {
            String homePageUrl = getHomePageUrl(request);
         // Get the Part representing the file upload
            Part filePart = request.getPart("file");
            // Get the file name
            String fileName = getFileName(filePart);
            // Read the file content
            InputStream fileContent = filePart.getInputStream();
            // Call your existing Java method
            filePathCapture(homePageUrl, fileContent, fileName); 
            // Send a success response
            response.getWriter().write("Images Downloaded Successfully!!!");
        } catch (Exception e) {
            // Handle the exception or log it
            e.printStackTrace();
            // Send an error response
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected Error Occurred. Please re-try.");
        }
    }

    private String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
    
    private String getHomePageUrl(HttpServletRequest request) {
        String homePageUrl = null;

        try {
            // Check if it's a multi-part form data request
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                if (part.getName().equals("homePageUrl")) {
                    homePageUrl = extractField(part);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception
        }

        return homePageUrl;
    }

    private String extractField(Part part) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception
        }

        return null;
    }


	private void filePathCapture(String homePageUrl, InputStream fileContent, String fileName) {
		try {
			List<String> itemCodeList = executeItemCode(fileContent, fileName);
			imageDownload(itemCodeList, homePageUrl);
		} catch (Exception e) {
			// Handle the exception or log it
			e.printStackTrace();
		}
	}
	
	private List<String> executeItemCode(InputStream fileContent, String fileName) throws IOException {
	    List<String> itemCodeList = new ArrayList<>();

	    if (isSupportedFile(fileName)) {
	        try {
	            if (fileName.toLowerCase().endsWith(".csv")) {
	                // For CSV files, read the content directly without using WorkbookFactory
	                itemCodeList = readCSV(fileContent);
	            }
	        } catch (Exception exception) {
	            // Handle the exception or log it
	            exception.printStackTrace();
	            throw new IOException("Error reading file: " + exception.getMessage());
	        }
	    } else {
	        throw new IOException("Unsupported file type: Not an Excel or CSV file");
	    }

	    return itemCodeList;
	}

	private List<String> readCSV(InputStream fileContent) throws IOException {
	    List<String> itemCodeList = new ArrayList<>();

	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileContent))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            // Assuming item codes are in the first column
	            String[] parts = line.split(",");
	            if (parts.length > 0) {
	                String itemCode = parts[0].trim();
	                itemCodeList.add(itemCode);
	            }
	        }
	    }

	    return itemCodeList;
	}

	private boolean isSupportedFile(String fileName) {
	    String lowerCaseFileName = fileName.toLowerCase();
	    return lowerCaseFileName.endsWith(".csv");
	}

    private void imageDownload(List<String> itemCodeList, String homePageUrl) {
		try {
			itemCodeList.forEach(itemCode -> {
				try {
					 WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
					 ChromeOptions options = new ChromeOptions();
					 options.addArguments("--headless");
//					 options.setBinary(System.getenv("GOOGLE_CHROME_SHIM"));
				        WebDriver driver = new ChromeDriver();
					List<String> imagesList = new ArrayList<>();
					
					driver.navigate().to(homePageUrl + itemCode);
					driver.manage().window().maximize();
					WebDriverWait wait = new WebDriverWait(driver, 3600);
					System.out.println("Waiting for element to click...");
					WebElement gridElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".image-grid-col50")));					
//					WebDriverWait wait = new WebDriverWait(driver, 10); // Adjust the timeout as needed
//					WebElement gridElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".image-grid-col50")));
//					driver.findElement(By.className("image-grid-col50")).click();	
					gridElement.click();
					driver.findElement(By.className("desktop-image-zoom-thumbnail-button")).click();
					List<WebElement> buttonList = driver
							.findElements(By.className("desktop-image-zoom-thumbnail-button"));

					for (int element = 1; element <= buttonList.size(); element++) {
						driver.findElement(By.className("desktop-image-zoom-image-container")).click();
						imagesList.add(driver.findElement(By.className("desktop-image-zoom-primary-image"))
								.getAttribute("src"));
						Actions actions = new Actions(driver);
						WebElement nextButton = driver.findElement(By.className("desktop-image-zoom-next"));
						actions.moveToElement(nextButton).perform();
						driver.findElement(By.className("desktop-image-zoom-next")).click();
					}

					String defaultPath = System.getProperty("user.home") + "/Downloads/" + itemCode;
					File directory = new File(defaultPath);

					try {
						if (directory.exists()) {
							if (directory.isDirectory()) {
								// If the directory already exists and is a directory, delete it
								deleteDir(directory);
							} else {
								throw new FileAlreadyExistsException(
										"A file with the same name exists: " + directory.getPath());
							}
						}

						// Create the directory and its parents if they do not exist
						Files.createDirectories(directory.toPath());
						System.out.println("Directory created successfully: " + directory.getPath());

						int count = 1;
						for (String image : imagesList) {
							try (InputStream in = new URL(image).openStream()) {
							    Files.copy(in, Paths.get(defaultPath, count + ".jpg"));
							    count++;
							} catch (Exception exception) {
							    exception.printStackTrace();
							}
						}

					} catch (IOException e) {
						e.printStackTrace(); // Handle the exception according to your application's needs
					} finally {
						 driver.quit();
					}

				} catch (Exception exception) {
					// Handle exception
					exception.printStackTrace();
				}
			});
		} catch (Exception exception) {
			// Handle exception
			exception.printStackTrace();
		}
	}

	private void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}
}

