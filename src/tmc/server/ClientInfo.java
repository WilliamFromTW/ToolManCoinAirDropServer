package tmc.server;

public class ClientInfo {

	private String sWallet = null;
	private String sWalletPublicKey = null;
	private int iAirDropNumber = 0;
	
	public ClientInfo() {};
	
	public ClientInfo(String sWallet,String sWalletPublicKey) {
		this.sWallet = sWallet;
		this.sWalletPublicKey = sWalletPublicKey;
		
	}

	public void setWallet(String sWallet) {
		this.sWallet = sWallet;
	}
	public String getWallet() {
		return this.sWallet;
	}
	public void setWalletPublicKey(String sPublicKey) {
		this.sWalletPublicKey = sPublicKey;
	}
	public String getWalletPublicKey() {
		return this.sWalletPublicKey;
	}

	
	public int getAirDropNumber() {
		return iAirDropNumber;
	}
	
	public void setAirDropNumber(int iNumber) {
      this.iAirDropNumber = iNumber;
	}
	
	public void increase(int iNumber) {
		this.iAirDropNumber = iAirDropNumber + iNumber;
	}
	
}
