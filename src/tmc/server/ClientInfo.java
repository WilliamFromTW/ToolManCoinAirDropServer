package tmc.server;

public class ClientInfo {

	private String sWallet = null;
	private String sWalletPublicKey = null;
	private int iTransferCoinNumber = Integer.parseInt( ServerGlobal.TRANSFER_COIN_NUMBER );
	private int iAirDropNumber = 0;
	private boolean bIsAirDropped = false;
	
	public ClientInfo() {};
	
	public ClientInfo(String sWallet,String sWalletPublicKey) {
		this.sWallet = sWallet;
		this.sWalletPublicKey = sWalletPublicKey;
		
	}

	public void setAirDropped(boolean b) {
		this.bIsAirDropped = b;
	}
	public boolean getAirDropped() {
		return this.bIsAirDropped;
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
	
	public int getTranferCoinNumber() {
		return iTransferCoinNumber;
	}
	
	public void setTranferCoinNumber(int iNumber) {
      this.iTransferCoinNumber = iNumber;
	}

	public void increase(int iNumber) {
		this.iAirDropNumber = iAirDropNumber + iNumber;
	}
	
}
