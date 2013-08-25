package net.dirbaio.cryptocat.service;

import com.google.gson.*;

import java.lang.reflect.Type;

public class GsonHelper
{
	public static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
			new ByteArrayToBase64TypeAdapter()).create();

	private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]>
	{

		@Override
		public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			return Utils.fromBase64(json.getAsString());
		}

		@Override
		public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context)
		{
			return new JsonPrimitive(Utils.toBase64(src));
		}
	}
}