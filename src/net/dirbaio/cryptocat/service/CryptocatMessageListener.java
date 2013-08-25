package net.dirbaio.cryptocat.service;

/**
 * Created with IntelliJ IDEA.
 * User: dirbaio
 * Date: 8/17/13
 * Time: 5:49 AM
 * To change this template use File | Settings | File Templates.
 */
public interface CryptocatMessageListener
{
	public void messageReceived(CryptocatMessage message);
}
