package org.apache.activemq.store.kahadb.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.activemq.protobuf.Buffer;
import org.apache.activemq.protobuf.CodedInputStream;
import org.apache.activemq.protobuf.CodedOutputStream;
import org.apache.activemq.protobuf.InvalidProtocolBufferException;

public final class KahaDestination extends KahaDestinationBase<KahaDestination> {
	public ArrayList<String> missingFields() {
		ArrayList missingFields = super.missingFields();
		if (!hasType()) {
			missingFields.add("type");
		}
		if (!hasName()) {
			missingFields.add("name");
		}
		return missingFields;
	}
	public void clear() {
		super.clear();
		clearType();
		clearName();
	}
	public KahaDestination clone() {
		return new KahaDestination().mergeFrom(this);
	}
	public KahaDestination mergeFrom(KahaDestination other) {
		if (other.hasType()) {
			setType(other.getType());
		}
		if (other.hasName()) {
			setName(other.getName());
		}
		return this;
	}
	public int serializedSizeUnframed() {
		if (this.memoizedSerializedSize != -1) {
			return this.memoizedSerializedSize;
		}
		int size = 0;
		if (hasType()) {
			size += CodedOutputStream.computeEnumSize(1, getType().getNumber());
		}
		if (hasName()) {
			size += CodedOutputStream.computeStringSize(2, getName());
		}
		this.memoizedSerializedSize = size;
		return size;
	}
	public KahaDestination mergeUnframed(CodedInputStream input) throws IOException {
		while (true) {
			int tag = input.readTag();
			if ((tag & 0x7) == 4) {
				return this;
			}
			switch (tag) {
				case 0:
					return this;
				default:
					break;
				case 8:
					int t = input.readEnum();
					DestinationType value = DestinationType.valueOf(t);
					if (value != null) {
						setType(value);
					}
					break;
				case 18:
					setName(input.readString());
			}
		}
	}
	public void writeUnframed(CodedOutputStream output) throws IOException {
		if (hasType()) {
			output.writeEnum(1, getType().getNumber());
		}
		if (hasName())
			output.writeString(2, getName());
	}
	public static KahaDestination parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaDestination) new KahaDestination().mergeUnframed(data).checktInitialized();
	}
	public static KahaDestination parseUnframed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaDestination) ((KahaDestination) new KahaDestination().mergeUnframed(data)).checktInitialized();
	}
	public static KahaDestination parseUnframed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaDestination) ((KahaDestination) new KahaDestination().mergeUnframed(data)).checktInitialized();
	}
	public static KahaDestination parseUnframed(InputStream data) throws InvalidProtocolBufferException, IOException {
		return (KahaDestination) ((KahaDestination) new KahaDestination().mergeUnframed(data)).checktInitialized();
	}
	public static KahaDestination parseFramed(CodedInputStream data) throws InvalidProtocolBufferException, IOException {
		return (KahaDestination) ((KahaDestination) new KahaDestination().mergeFramed(data)).checktInitialized();
	}
	public static KahaDestination parseFramed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaDestination) ((KahaDestination) new KahaDestination().mergeFramed(data)).checktInitialized();
	}
	public static KahaDestination parseFramed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaDestination) ((KahaDestination) new KahaDestination().mergeFramed(data)).checktInitialized();
	}
	public static KahaDestination parseFramed(InputStream data) throws InvalidProtocolBufferException, IOException {
		return (KahaDestination) ((KahaDestination) new KahaDestination().mergeFramed(data)).checktInitialized();
	}
	public String toString() {
		return toString(new StringBuilder(), "").toString();
	}
	public StringBuilder toString(StringBuilder sb, String prefix) {
		if (hasType()) {
			sb.append(prefix + "type: ");
			sb.append(getType());
			sb.append("\n");
		}
		if (hasName()) {
			sb.append(prefix + "name: ");
			sb.append(getName());
			sb.append("\n");
		}
		return sb;
	}
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != KahaDestination.class)) {
			return false;
		}
		return equals((KahaDestination) obj);
	}
	public boolean equals(KahaDestination obj) {
		if ((hasType() ^ obj.hasType()))
			return false;
		if ((hasType()) && (!getType().equals(obj.getType())))
			return false;
		if ((hasName() ^ obj.hasName()))
			return false;
		if ((hasName()) && (!getName().equals(obj.getName())))
			return false;
		return true;
	}
	public int hashCode() {
		int rc = -972308577;
		if (hasType()) {
			rc ^= 0x28035A ^ getType().hashCode();
		}
		if (hasName()) {
			rc ^= 0x24EEAB ^ getName().hashCode();
		}
		return rc;
	}

	/**
	 * 使用枚举类型表示queue，topic，tempQueue和tempTopic
	 * @ClassName: DestinationType
	 * @Description: TODO
	 * @author: 孔新
	 * @date: 2016年8月14日 下午3:06:48
	 */
	public static enum DestinationType {
		QUEUE("QUEUE", 0), TOPIC("TOPIC", 1), TEMP_QUEUE("TEMP_QUEUE", 2), TEMP_TOPIC("TEMP_TOPIC", 3);
		private final String name;
		private final int value;

		private DestinationType(String name, int value) {
			this.name = name;
			this.value = value;
		}
		public final int getNumber() {
			return this.value;
		}
		public final String toString() {
			return this.name;
		}
		public static DestinationType valueOf(int value) {
			switch (value) {
				case 0:
					return QUEUE;
				case 1:
					return TOPIC;
				case 2:
					return TEMP_QUEUE;
				case 3:
					return TEMP_TOPIC;
			}
			return null;
		}
	}
}
