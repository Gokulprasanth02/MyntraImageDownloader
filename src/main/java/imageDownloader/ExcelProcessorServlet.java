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
import org.openqa.selenium.interactions.Actions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

/*@SuppressWarnings("serial")
@WebServlet("/processExcel")
public class ExcelProcessorServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        
        try {
			// Retrieve parameters from the request
			String homePageUrl = request.getParameter("homePageUrl");
			String itemCodeFilePath = request.getParameter("itemCodeFilePath");

			// Call your existing Java method
			filePathCapture(homePageUrl, itemCodeFilePath);

			// Send a success response
			response.getWriter().write("Images Downloaded Successfully!!!");
		} catch (Exception e) {
			// Handle the exception or log it
			e.printStackTrace();
			// Send an error response
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Unexpected Error Occurred. Please re-try.");
		}
    }*/

@SuppressWarnings("serial")
@MultipartConfig(maxFileSize = 1024 * 1024 * 5) // Set to 5 MB, adjust as needed
@WebServlet("/processExcel")
public class ExcelProcessorServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
     // Example CORS configuration in a Java servlet
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
     // Example setting response headers in a Java servlet
//        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        try {
            // Retrieve JSON data from the request body
//            BufferedReader reader = request.getReader();
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//            String jsonData = sb.toString();
            String homePageUrl = getHomePageUrl(request);

            // Parse JSON data
//            JSONObject jsonObject = new JSONObject(jsonData);

            // Retrieve parameters from the JSON object
//            String homePageUrl = jsonObject.getString("homePageUrl");
//            String itemCodeFilePath = jsonObject.getString("itemCodeFilePath");
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
            System.out.println(parts);
            for (Part part : parts) {
                if (part.getName().equals("homePageUrl")) {
                    homePageUrl = extractField(part);
                    // Now you have the value of homePageUrl
                    System.out.println("homePageUrl: " + homePageUrl);
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
	            } else {
//	                // For Excel files, use WorkbookFactory
//	                try (Workbook workbook = WorkbookFactory.create(fileContent)) {
//	                    Sheet sheet = workbook.getSheetAt(0);
//	                    Iterator<Row> iterator = sheet.iterator();
//	                    while (iterator.hasNext()) {
//	                        Row currentRow = iterator.next();
//	                        // Assuming item codes are in the first column
//	                        Cell cell = currentRow.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
//
//	                        if (cell != null && cell.getCellType() == CellType.STRING) {
//	                            String itemCode = cell.getStringCellValue().trim();
//	                            itemCodeList.add(itemCode);
//	                        }
//	                    }
//	                }
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

	
//	private List<String> executeItemCode(InputStream fileContent, String fileName) throws IOException {
//	    List<String> itemCodeList = new ArrayList<>();
//
//	    if (isSupportedFile(fileName)) {
//	        try (Workbook workbook = WorkbookFactory.create(fileContent)) {
//	            Sheet sheet = workbook.getSheetAt(0);
//	            Iterator<Row> iterator = sheet.iterator();
//	            while (iterator.hasNext()) {
//	                Row currentRow = iterator.next();
//	                // Assuming item codes are in the first column
//	                Cell cell = currentRow.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
//
//	                if (cell != null && cell.getCellType() == CellType.STRING) {
//	                    String itemCode = cell.getStringCellValue().trim();
//	                    itemCodeList.add(itemCode);
//	                }
//	            }
//	        } catch (Exception exception) {
//	            // Handle the exception or log it
//	            exception.printStackTrace();
//	            throw new IOException("Error reading Excel file: " + exception.getMessage());
//	        }
//	    } else {
//	        throw new IOException("Unsupported file type: Not an Excel or CSV file");
//	    }
//
//	    return itemCodeList;
//	}
//
//	private boolean isSupportedFile(String fileName) {
//	    String lowerCaseFileName = fileName.toLowerCase();
//	    return lowerCaseFileName.endsWith(".xlsx") || lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".csv");
//	}

	
//	private List<String> executeItemCode(InputStream fileContent, String fileName) throws IOException {
//	    List<String> itemCodeList = new ArrayList<>();
//
//	    try (Workbook workbook = WorkbookFactory.create(fileContent)) {
//	        Sheet sheet = workbook.getSheetAt(0);
//	        Iterator<Row> iterator = sheet.iterator();
//	        while (iterator.hasNext()) {
//	            Row currentRow = iterator.next();
//	            // Assuming item codes are in the first column
//	            Cell cell = currentRow.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
//
//	            if (cell != null && cell.getCellType() == CellType.STRING) {
//	                String itemCode = cell.getStringCellValue().trim();
//	                itemCodeList.add(itemCode);
//	            }
//	        }
//	    } catch (Exception exception) {
//	        // Handle the exception or log it
//	        exception.printStackTrace();
//	        throw new IOException("Error reading Excel file: " + exception.getMessage());
//	    }
//
//	    return itemCodeList;
//	}

	
//	private List<String> executeItemCode(InputStream fileContent, String fileName) throws IOException {
//	    List<String> itemCodeList = new ArrayList<>();
//
//	    // Check if the file has a valid Excel or CSV file extension
//	    if (!fileTypeIsExcelOrCSV(fileContent, fileName)) {
//	        throw new IOException("Unsupported file type: Not an Excel or CSV file");
//	    }
//
//	    try (Workbook workbook = WorkbookFactory.create(fileContent)) {
//	        Sheet sheet = workbook.getSheetAt(0);
//	        Iterator<Row> iterator = sheet.iterator();
//	        while (iterator.hasNext()) {
//	            Row currentRow = iterator.next();
//	            // Assuming item codes are in the first column
//	            Cell cell = currentRow.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
//
//	            if (cell != null && cell.getCellType() == CellType.STRING) {
//	                String itemCode = cell.getStringCellValue().trim();
//	                itemCodeList.add(itemCode);
//	            }
//	        }
//	    } catch (Exception exception) {
//	        exception.printStackTrace();
//	    }
//
//	    return itemCodeList;
//	}

	
//	private List<String> executeItemCode(InputStream fileContent, String fileName) throws IOException {
//	    List<String> itemCodeList = new ArrayList<>();
//
//	    // Check if the file has a valid Excel or CSV file extension
//	    if (!fileTypeIsExcelOrCSV(fileContent, fileName)) {
//	        throw new IOException("Unsupported file type: Not an Excel or CSV file");
//	    }
//
////	    try (Workbook workbook = WorkbookFactory.create(fileContent)) {
////	    try (InputStream inputStreamForWorkbook = new ByteArrayInputStream(fileContent.readAllBytes());
//	    // Read all bytes from the original fileContent stream
//	    byte[] fileBytes = fileContent.readAllBytes();
//
//	    // Create a new InputStream using the bytes obtained
//	    try (InputStream inputStreamForWorkbook = new ByteArrayInputStream(fileBytes);
//	         Workbook workbook = WorkbookFactory.create(inputStreamForWorkbook)) {
////	            Workbook workbook = WorkbookFactory.create(inputStreamForWorkbook)) {
//	        Sheet sheet = workbook.getSheetAt(0);
//	        Iterator<Row> iterator = sheet.iterator();
//	        while (iterator.hasNext()) {
//	            Row currentRow = iterator.next();
//	            // Assuming item codes are in the first column
//	            Cell cell = currentRow.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
//
//	            if (cell != null && cell.getCellType() == CellType.STRING) {
//	                String itemCode = cell.getStringCellValue().trim();
//	                itemCodeList.add(itemCode);
//	            }
//	        }
//	    } catch (Exception exception) {
//	        exception.printStackTrace();
//	    }
//
//	    return itemCodeList;
//	}

	
//	private List<String> executeItemCode(InputStream fileContent, String fileName) throws IOException {
//	    List<String> itemCodeList = new ArrayList<>();
//
//	    try (BufferedInputStream bis = new BufferedInputStream(fileContent);
//	         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
//
//	        // Check if the file is not empty
//	        if (bis.available() == 0) {
//	            throw new IOException("The supplied file is empty");
//	        }
//
//	        // Check if the file has a valid Excel or CSV file extension
//	        if (!fileTypeIsExcelOrCSV(bis, fileName)) {
//	            throw new IOException("Unsupported file type: Not an Excel or CSV file");
//	        }
//
//	        // Read all bytes into the ByteArrayOutputStream
//	        byte[] buffer = new byte[1024];
//	        int bytesRead;
//	        while ((bytesRead = bis.read(buffer)) != -1) {
//	            byteArrayOutputStream.write(buffer, 0, bytesRead);
//	        }
//
//	        // Use the ByteArrayOutputStream to create a new ByteArrayInputStream
//	        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//	             Workbook workbook = WorkbookFactory.create(byteArrayInputStream)) {
//	            Sheet sheet = workbook.getSheetAt(0);
//	            Iterator<Row> iterator = sheet.iterator();
//	            while (iterator.hasNext()) {
//	                Row currentRow = iterator.next();
//	                // Assuming item codes are in the first column
//	                Cell cell = currentRow.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
//
//	                if (cell != null && cell.getCellType() == CellType.STRING) {
//	                    String itemCode = cell.getStringCellValue().trim();
//	                    itemCodeList.add(itemCode);
//	                }
//	            }
//	        } catch (Exception exception) {
//	            exception.printStackTrace();
//	        }
//	    }
//
//	    return itemCodeList;
//	}


//	private List<String> executeItemCode(InputStream fileContent, String fileName) throws IOException {
//	    List<String> itemCodeList = new ArrayList<>();
//
//	    // Check if the file is not empty
//	    if (fileContent.available() == 0) {
//	        throw new IOException("The supplied file is empty");
//	    }
//
//	    // Check if the file has a valid Excel or CSV file extension
//	    if (!fileTypeIsExcelOrCSV(fileContent, fileName)) {
//	        throw new IOException("Unsupported file type: Not an Excel or CSV file");
//	    }
//
//	    // Reset the InputStream to the beginning
//	    fileContent.mark(Integer.MAX_VALUE);
//        
//	    try (BufferedInputStream bis = new BufferedInputStream(fileContent)) {
//	        // Read all bytes into an array
//	        byte[] fileContentBytes = bis.readAllBytes();
//
//	        // Use the array to create a new ByteArrayInputStream
//	        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileContentBytes);
//	             Workbook workbook = WorkbookFactory.create(byteArrayInputStream)) {
//	            Sheet sheet = workbook.getSheetAt(0);
//	            Iterator<Row> iterator = sheet.iterator();
//	            while (iterator.hasNext()) {
//	                Row currentRow = iterator.next();
//	                // Assuming item codes are in the first column
//	                Cell cell = currentRow.getCell(0, MissingCellPolicy.RETURN_BLANK_AS_NULL);
//
//	                if (cell != null && cell.getCellType() == CellType.STRING) {
//	                    String itemCode = cell.getStringCellValue().trim();
//	                    itemCodeList.add(itemCode);
//	                }
//	            }
//	        } catch (Exception exception) {
//	            exception.printStackTrace();
//	        }
//	    }
//
//	    return itemCodeList;
//	}

    
//		List<String> itemCodeList = new ArrayList<>();
//
//		try (Workbook workbook = WorkbookFactory.create(fileContent)) {
//		    Sheet sheet = workbook.getSheetAt(0); // Assuming item codes are in the first sheet
//
//		    Iterator<Row> iterator = sheet.iterator();
//		    while (iterator.hasNext()) {
//		        Row currentRow = iterator.next();
//		        Cell cell = currentRow.getCell(0); // Assuming item codes are in the first column
//		        String itemCode = cell.getStringCellValue();
//		        itemCodeList.add(itemCode);
//		    }
//		} catch (Exception exception) {
//		    // Handle the exception or log it
//		    exception.printStackTrace();
//		}
//
//		return itemCodeList;

//		try (FileInputStream excelFile = new FileInputStream(new File(fileContent))) {
//			Workbook workbook = WorkbookFactory.create(excelFile);
//			Sheet sheet = workbook.getSheetAt(0); // Assuming item codes are in the first sheet
//
//			Iterator<Row> iterator = sheet.iterator();
//			while (iterator.hasNext()) {
//				Row currentRow = iterator.next();
//				Cell cell = currentRow.getCell(0); // Assuming item codes are in the first column
//				String itemCode = cell.getStringCellValue();
//				itemCodeList.add(itemCode);
//			}
//		} catch (Exception exception) {
//			// Handle the exception or log it
//			exception.printStackTrace();
//		}
//
//		return itemCodeList;
//	}
    
//    private boolean fileTypeIsExcelOrCSV(InputStream fileContent, String fileName) throws IOException {
//        try (BufferedInputStream bis = new BufferedInputStream(fileContent)) {
//            byte[] fileSignature = new byte[4];
//            bis.mark(4);
//            bis.read(fileSignature, 0, 4);
//            bis.reset();
//
//            // Check if the file signature matches the typical Excel signatures
//            boolean isExcel = (fileSignature[0] == 0x50 && fileSignature[1] == 0x4B) || // ZIP
//                              (fileSignature[0] == 0xD0 && fileSignature[1] == 0xCF);    // Old Excel format
//
//            // Check if the file has a valid Excel or CSV file extension
//            boolean isXLSX = isExcel && (fileSignature[2] == 0x4B && fileSignature[3] == 0x50); // .xlsx
//            boolean isXLS = isExcel && (fileSignature[2] == 0x06 && fileSignature[3] == 0x10);  // .xls
//            boolean isCSV = !isExcel && fileName.toLowerCase().endsWith(".csv");
//
//            return isXLSX || isXLS || isCSV;
//        }
//    }


    
//    private boolean fileTypeIsExcel(InputStream fileContent) throws IOException {
//        try (BufferedInputStream bis = new BufferedInputStream(fileContent)) {
//            byte[] fileSignature = new byte[4];
//            bis.mark(4);
//            bis.read(fileSignature, 0, 4);
//            bis.reset();
//
//            // Check if the file signature matches the typical Excel signatures or CSV
//            return (fileSignature[0] == 0x50 && fileSignature[1] == 0x4B) || // ZIP (Excel)
//                   (fileSignature[0] == 0xD0 && fileSignature[1] == 0xCF) ||    // Old Excel format
//                   (fileSignature[0] == 0x2C && fileSignature[1] == 0x2E);       // CSV
//        }
//    }
        

    private void imageDownload(List<String> itemCodeList, String homePageUrl) {
		try {
			itemCodeList.forEach(itemCode -> {
				try {
//					System.setProperty("webdriver.chrome.driver", "C:\\ChromeDriver\\chromedriver.exe");
					// WebDriverManager.chromedriver().setup();
					 WebDriverManager.getInstance(DriverManagerType.CHROME).setup();
				        WebDriver driver = new ChromeDriver();
//					WebDriver driver = new ChromeDriver();
//					List<String> imagesList = new ArrayList<>();

//					WebDriverManager.chromedriver().setup();
//					System.setProperty("webdriver.gecko.driver", "C:\\geckodriver\\geckodriver.exe");
//					WebDriver driver = new FirefoxDriver();
//					DesiredCapabilities capabilities = DesiredCapabilities.firefox();
//					capabilities.setCapability("marionette",true);
//					driver= new FirefoxDriver(capabilities);
					//WebDriver driver = new ChromeDriver();
					List<String> imagesList = new ArrayList<>();
					
					driver.navigate().to(homePageUrl + itemCode);
					driver.manage().window().maximize();
					driver.findElement(By.className("image-grid-col50")).click();
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
//						driver.close();
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

