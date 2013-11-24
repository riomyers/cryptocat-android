package net.dirbaio.cryptocat.service;

public class CryptocatMessage
{
	public final Type t;
	public final String nickname;
	public final String text;

	public CryptocatMessage(String nickname, Type t)
	{
		this.nickname = nickname;
		this.t = t;
		this.text = null;
	}

	public CryptocatMessage(Type t, String nickname, String text)
	{
		this.t = t;
		this.nickname = nickname;
		this.text = text;
	}

	public enum Type
	{
		Message,
		Join,
		Leave,
        File,
        Error
	}
}
