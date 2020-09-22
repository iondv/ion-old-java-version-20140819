package ion.offline.mocks;

import java.io.File;
import java.io.IOException;

import ion.offline.util.IVolumeProcessor;

public class VolumeProcessorMock implements IVolumeProcessor {
	
	private File[] volumes;
	
	public VolumeProcessorMock(File[] volumes) {
		super();
		this.volumes = volumes;
	}

	@Override
	public File[] Split(File src, File dest) throws IOException {return volumes;}

	@Override
	public void Join(File[] volumes, File dest) throws IOException {
		// TODO Auto-generated method stub
	}

}
