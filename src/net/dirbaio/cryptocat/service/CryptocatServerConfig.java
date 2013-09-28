package net.dirbaio.cryptocat.service;

public class CryptocatServerConfig
{
	public final String server, conferenceServer;
	public final String boshRelay;
	public final int port;
	public final boolean useBosh;

	public CryptocatServerConfig(String server, String conferenceServer, String boshRelay)
	{
		this.server = server;
		this.conferenceServer = conferenceServer;
		this.boshRelay = boshRelay;
		this.port = -1;
		this.useBosh = true;
	}

	public CryptocatServerConfig(String server, String conferenceServer, int port)
	{
		this.server = server;
		this.conferenceServer = conferenceServer;
		this.boshRelay = null;
		this.port = port;
		this.useBosh = false;
	}
}
