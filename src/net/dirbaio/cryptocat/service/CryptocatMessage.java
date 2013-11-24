package net.dirbaio.cryptocat.service;

public class CryptocatMessage
{
	public final Type type;
	public final String nickname;
	public final String text;

	public CryptocatMessage(String nickname, Type type)
	{
		this.nickname = nickname;
		this.type = type;
		this.text = null;
	}

	public CryptocatMessage(Type type, String nickname, String text)
	{
		this.type = type;
		this.nickname = nickname;
		this.text = text;
	}

	public enum Type
	{
		Message,
        MessageMine,
		Join,
		Leave,
        File,
        Error
	}
}
