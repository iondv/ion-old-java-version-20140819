package ion.offline.server.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "point")
public class Point {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "id")
    private Integer id = null;
	
    @Column(name = "open_key")
    private String openKey = null;    
        
    @Column(name = "last_data_sending")
    private Date lastDataSending = null;

    @Column(name = "last_data_package_generating")
    private Date lastDataPackageGenerating = null;

    @Column(name = "data_hash")
    private String dataHash = null;
    
    @Column(name = "authorization_token")
    private String authorizationToken = null;
    
    @Column(name = "sync_horizon")
    private Integer syncHorizon = null;

		public Point(){
    	
    }
    
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getOpenKey() {
		return openKey;
	}

	public void setOpenKey(String openKey) {
		this.openKey = openKey;
	}

	public Date getLastDataSending() {
		return lastDataSending;
	}

	public void setLastDataSending(Date lastDataSending) {
		this.lastDataSending = lastDataSending;
	}

	public Date getLastDataPackageGenerating() {
		return lastDataPackageGenerating;
	}

	public void setLastDataPackageGenerating(Date lastDataPackageGenerating) {
		this.lastDataPackageGenerating = lastDataPackageGenerating;
	}

	public String getDataHash() {
		return dataHash;
	}

	public void setDataHash(String dataHash) {
		this.dataHash = dataHash;
	}

	public String getAuthorizationToken() {
		return authorizationToken;
	}

	public void setAuthorizationToken(String authorizationToken) {
		this.authorizationToken = authorizationToken;
	}
	
  public Integer getSyncHorizon() {
		return syncHorizon;
	}

	public void setSyncHorizon(Integer syncHorizon) {
		this.syncHorizon = syncHorizon;
	}
}
