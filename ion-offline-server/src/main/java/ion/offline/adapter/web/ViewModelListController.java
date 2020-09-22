package ion.offline.adapter.web;

import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.offline.adapter.data.ViewModelInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping(value = "/viewmodels")
public class ViewModelListController extends BasicController {
	
  @Value("${app.viewModelDir}")
  private String vmDir;	
	
	@Autowired
	private ILogger logger;

  protected File getVmDir(HttpServletRequest request) throws IOException {
  	File f = new File(vmDir);
  	if (f.exists())
  		return f;
  	ServletContext context = request.getServletContext();
  	String realpath = context.getRealPath(vmDir);
  	if (realpath == null)
  			return new ServletContextResource(context, vmDir).getFile();
  	//	throw new IonException("По указанному пути \"" + vmDir + "\" не найдена директория моделей представления.");
  	return new File(realpath);
  }

	
	@RequestMapping(value = "", method={RequestMethod.GET})
	public String list(HttpServletRequest request, Model model) throws IOException{
		File vmDir = getVmDir(request);
		List<ViewModelInfo> models = new LinkedList<ViewModelInfo>();
		
		DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM);
		
		for(File f: vmDir.listFiles()){
			if (f.isDirectory()){
				String cn = f.getName();
				File im = new File(f,"item.json");
				if (im.exists())
					models.add(new ViewModelInfo(cn, "форма", df.format(im.lastModified()), AppRoot()+"/vm/"+cn+"/item.json"));
				File lm = new File(f,"list.json");
				if (lm.exists())
					models.add(new ViewModelInfo(cn, "список", df.format(lm.lastModified()), AppRoot()+"/vm/"+cn+"/list.json"));
			}
		}
			
		model.addAttribute("models", models);
		model.addAttribute("Title", "Модели представления");
		return themeDir+"/viewmodels";
	}
	
	private void packModels(File src, File base, ZipOutputStream archive_stream) throws IOException {
		if(src.isDirectory()){
			for(File f:src.listFiles())
				packModels(f, base, archive_stream);
		} else if (src.isFile()) {
			FileInputStream file_stream = new FileInputStream(src);
			String path = base.toURI().relativize(src.toURI()).getPath();			
			ZipEntry entry = new ZipEntry((path.length() > 0)?path:src.getName());
			archive_stream.putNextEntry(entry);
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = file_stream.read(buffer)) > 0) {
				archive_stream.write(buffer, 0, count);
			}
			archive_stream.closeEntry();
			file_stream.close();
		}
	}	
	
	@RequestMapping(value = "/pack", method={RequestMethod.GET}, produces="application/zip")
	public void downloadPackage(HttpServletRequest request, HttpServletResponse response) throws IOException{
	  response.setContentType("application/zip");
	  response.addHeader("Content-Disposition", "attachment; filename=\"package.zip\"");
	  response.addHeader("Content-Transfer-Encoding", "binary");
		File vmDirectory = getVmDir(request);
		ZipOutputStream archive_stream = new ZipOutputStream(response.getOutputStream());
		archive_stream.setLevel(9);
		packModels(vmDirectory, vmDirectory, archive_stream);
		archive_stream.close();
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	@RequestMapping(value = "/uploadModel/{className}", method={RequestMethod.POST})
	@ResponseStatus(value = HttpStatus.OK)
	//@ResponseBody
	public void uploadModel(@PathVariable String className, MultipartHttpServletRequest request, HttpServletResponse response) 
			throws IonException, IOException {
		try {
			MultipartFile f = request.getFile("data");
			File dest = new File(getVmDir(request),className);
			File volume = new File(dest, f.getOriginalFilename());
			f.transferTo(volume);
		} catch(Exception e) {
			logger.Error("Ошибка обработки запроса!", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IonException(e);
		}
	}
	
	@Transactional(
		propagation = Propagation.REQUIRED,
		rollbackFor = { IonException.class })
	@RequestMapping(value = "/uploadPackage", method = { RequestMethod.POST })
	@ResponseStatus(value = HttpStatus.OK)
	// @ResponseBody
	public void uploadPackage(MultipartHttpServletRequest request,
														HttpServletResponse response) throws IonException,
																												 IOException {
		try {
			MultipartFile f = request.getFile("data");
			File vmDirectory = getVmDir(request);
			InputStream input_stream = f.getInputStream();
			ZipInputStream archive_stream = new ZipInputStream(input_stream);
			try {
				ZipEntry entry;
				while ((entry = archive_stream.getNextEntry()) != null) {
					File file = new File(vmDirectory, entry.getName());
					if (entry.isDirectory()){
							file.mkdirs();
					} else {
							file.getParentFile().mkdirs();
							if (!file.exists())
								file.createNewFile();						
							FileOutputStream file_stream = new FileOutputStream(file);
							try {
								byte[] buffer = new byte[1024];
								int count = -1;
								while ((count = archive_stream.read(buffer)) > 0) {
										file_stream.write(buffer, 0, count);
								}
							} finally {
								file_stream.close();
							}
					}
				}
			} finally {
				archive_stream.close();
				input_stream.close();
			}
		} catch (Exception e) {
			logger.Error("Ошибка обработки запроса!", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IonException(e);
		}
	}
}
