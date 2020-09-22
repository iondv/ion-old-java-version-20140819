package ion.offline.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ion.offline.util.IVolumeProcessor;

public class ZipVolumeProcessor implements IVolumeProcessor {
	private int volumeSizeLimit;

	public int getVolumeSizeLimit() {
		return volumeSizeLimit;
	}

	public void setVolumeSizeLimit(int volumeSizeLimit) {
		this.volumeSizeLimit = volumeSizeLimit;
	}

	public ZipVolumeProcessor() {
	}

	public ZipVolumeProcessor(int limit) {
		this.volumeSizeLimit = limit;
	}

	private ArrayList<File> getFiles(File directory) {
		ArrayList<File> files = new ArrayList<File>();
		if (directory.isDirectory()) {
			for (String name : directory.list())
				files.addAll(getFiles(new File(directory, name)));
		} else {
			files.add(directory);
		}
		return files;
	}

	public File[] Split(File src, File dest) throws IOException {
		ArrayList<File> files = getFiles(src);
		String volume_name = src.getName();

		File base = src;
		if (src.isFile()) {
			volume_name = volume_name.substring(0, volume_name.lastIndexOf("."));
			base = src.getParentFile();
		}

		dest.mkdirs();

		File packet = new File(dest, volume_name + ".zip");
		
		FileOutputStream archive_fo_stream = new FileOutputStream(packet);
		ZipOutputStream archive_stream = new ZipOutputStream(archive_fo_stream);
		archive_stream.setLevel(9);
		for (File file : files) {
			FileInputStream file_stream = new FileInputStream(file);
			String path = base.toURI().relativize(file.toURI()).getPath();
			ZipEntry entry = new ZipEntry((path.length() > 0) ? path : file.getName());
			archive_stream.putNextEntry(entry);
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = file_stream.read(buffer)) > 0) {
				archive_stream.write(buffer, 0, count);
			}
			archive_stream.closeEntry();
			file_stream.close();
		}
		archive_stream.flush();
		archive_stream.close();

		File[] volumes;
		
		long pl = packet.length();
		
		if (volumeSizeLimit > 0 && volumeSizeLimit < pl) {
			FileInputStream fin = new FileInputStream(packet);
			int volumes_count = (int) Math.ceil(pl
					/ (double) volumeSizeLimit);
			volumes = new File[volumes_count];
			try {
  			for (int i = 0; i < volumes_count; i++) {
  				File volume = new File(dest, volume_name + ".vol" + i);
  				FileOutputStream volume_stream = new FileOutputStream(volume);
  				try {
  					byte[] data = new byte[volumeSizeLimit];
  					int ws = fin.read(data);
  					volume_stream.write(data,0,ws);
  				} finally {
  					volume_stream.close();
  				}
  				volumes[i] = volume;
  			}
			} finally {
				fin.close();
				packet.delete();
			}
		} else {
			volumes = new File[1];
			volumes[0] = packet;
		}
		return volumes;
	}

	public void Join(File[] volumes, File dest) throws IOException {
		dest.mkdirs();
		InputStream input_stream;
		ByteArrayOutputStream archive_ba_stream = new ByteArrayOutputStream();
		if (volumes.length > 1) {
			Arrays.sort(volumes, new Comparator<File>() {
				public int compare(File o1, File o2) {
					int ind1 = Integer.parseInt(o1.getName()
																				.substring(o1.getName()
																										 .lastIndexOf(".") + 1)
																				.replaceAll("[^\\d]", ""));
					int ind2 = Integer.parseInt(o2.getName()
																				.substring(o1.getName()
																										 .lastIndexOf(".") + 1)
																				.replaceAll("[^\\d]", ""));
					return ind1 - ind2;
				}
			});
		}

		for (File volume : volumes) {
			FileInputStream volume_stream = new FileInputStream(volume);
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = volume_stream.read(buffer)) > 0) {
				archive_ba_stream.write(buffer, 0, count);
			}
			volume_stream.close();
		}
		archive_ba_stream.close();
		input_stream = new ByteArrayInputStream(archive_ba_stream.toByteArray());
		ZipInputStream archive_stream = new ZipInputStream(input_stream);
		ZipEntry entry;

		while ((entry = archive_stream.getNextEntry()) != null) {
			File file = new File(dest, entry.getName());
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
		archive_stream.close();
	}
}
