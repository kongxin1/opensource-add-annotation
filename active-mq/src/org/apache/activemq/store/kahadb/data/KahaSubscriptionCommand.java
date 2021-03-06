package org.apache.activemq.store.kahadb.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.activemq.protobuf.Buffer;
import org.apache.activemq.protobuf.CodedInputStream;
import org.apache.activemq.protobuf.CodedOutputStream;
import org.apache.activemq.protobuf.InvalidProtocolBufferException;
import org.apache.activemq.protobuf.UninitializedMessageException;
import org.apache.activemq.store.kahadb.JournalCommand;
import org.apache.activemq.store.kahadb.Visitor;

public final class KahaSubscriptionCommand extends KahaSubscriptionCommandBase<KahaSubscriptionCommand> implements
		JournalCommand<KahaSubscriptionCommand> {
	public ArrayList<String> missingFields() {
		ArrayList missingFields = super.missingFields();
		if (!hasDestination()) {
			missingFields.add("destination");
		}
		if (!hasSubscriptionKey()) {
			missingFields.add("subscriptionKey");
		}
		if (hasDestination()) {
			try {
				getDestination().assertInitialized();
			} catch (UninitializedMessageException e) {
				missingFields.addAll(prefix(e.getMissingFields(), "destination."));
			}
		}
		return missingFields;
	}
	public void clear() {
		super.clear();
		clearDestination();
		clearSubscriptionKey();
		clearRetroactive();
		clearSubscriptionInfo();
	}
	public KahaSubscriptionCommand clone() {
		return new KahaSubscriptionCommand().mergeFrom(this);
	}
	public KahaSubscriptionCommand mergeFrom(KahaSubscriptionCommand other) {
		if (other.hasDestination()) {
			if (hasDestination())
				getDestination().mergeFrom(other.getDestination());
			else {
				setDestination(other.getDestination().clone());
			}
		}
		if (other.hasSubscriptionKey()) {
			setSubscriptionKey(other.getSubscriptionKey());
		}
		if (other.hasRetroactive()) {
			setRetroactive(other.getRetroactive());
		}
		if (other.hasSubscriptionInfo()) {
			setSubscriptionInfo(other.getSubscriptionInfo());
		}
		return this;
	}
	public int serializedSizeUnframed() {
		if (this.memoizedSerializedSize != -1) {
			return this.memoizedSerializedSize;
		}
		int size = 0;
		if (hasDestination()) {
			size += computeMessageSize(1, getDestination());
		}
		if (hasSubscriptionKey()) {
			size += CodedOutputStream.computeStringSize(2, getSubscriptionKey());
		}
		if (hasRetroactive()) {
			size += CodedOutputStream.computeBoolSize(3, getRetroactive());
		}
		if (hasSubscriptionInfo()) {
			size += CodedOutputStream.computeBytesSize(4, getSubscriptionInfo());
		}
		this.memoizedSerializedSize = size;
		return size;
	}
	public KahaSubscriptionCommand mergeUnframed(CodedInputStream input) throws IOException {
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
				case 10:
					if (hasDestination())
						getDestination().mergeFramed(input);
					else {
						setDestination((KahaDestination) new KahaDestination().mergeFramed(input));
					}
					break;
				case 18:
					setSubscriptionKey(input.readString());
					break;
				case 24:
					setRetroactive(input.readBool());
					break;
				case 34:
					setSubscriptionInfo(input.readBytes());
			}
		}
	}
	public void writeUnframed(CodedOutputStream output) throws IOException {
		if (hasDestination()) {
			writeMessage(output, 1, getDestination());
		}
		if (hasSubscriptionKey()) {
			output.writeString(2, getSubscriptionKey());
		}
		if (hasRetroactive()) {
			output.writeBool(3, getRetroactive());
		}
		if (hasSubscriptionInfo())
			output.writeBytes(4, getSubscriptionInfo());
	}
	public static KahaSubscriptionCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeUnframed(data).checktInitialized();
	}
	public static KahaSubscriptionCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaSubscriptionCommand) ((KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeUnframed(data))
				.checktInitialized();
	}
	public static KahaSubscriptionCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaSubscriptionCommand) ((KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeUnframed(data))
				.checktInitialized();
	}
	public static KahaSubscriptionCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaSubscriptionCommand) ((KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeUnframed(data))
				.checktInitialized();
	}
	public static KahaSubscriptionCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaSubscriptionCommand) ((KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeFramed(data))
				.checktInitialized();
	}
	public static KahaSubscriptionCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaSubscriptionCommand) ((KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeFramed(data))
				.checktInitialized();
	}
	public static KahaSubscriptionCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaSubscriptionCommand) ((KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeFramed(data))
				.checktInitialized();
	}
	public static KahaSubscriptionCommand parseFramed(InputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaSubscriptionCommand) ((KahaSubscriptionCommand) new KahaSubscriptionCommand().mergeFramed(data))
				.checktInitialized();
	}
	public String toString() {
		return toString(new StringBuilder(), "").toString();
	}
	public StringBuilder toString(StringBuilder sb, String prefix) {
		if (hasDestination()) {
			sb.append(prefix + "destination {\n");
			getDestination().toString(sb, prefix + "  ");
			sb.append(prefix + "}\n");
		}
		if (hasSubscriptionKey()) {
			sb.append(prefix + "subscriptionKey: ");
			sb.append(getSubscriptionKey());
			sb.append("\n");
		}
		if (hasRetroactive()) {
			sb.append(prefix + "retroactive: ");
			sb.append(getRetroactive());
			sb.append("\n");
		}
		if (hasSubscriptionInfo()) {
			sb.append(prefix + "subscriptionInfo: ");
			sb.append(getSubscriptionInfo());
			sb.append("\n");
		}
		return sb;
	}
	public void visit(Visitor visitor) throws IOException {
		visitor.visit(this);
	}
	public KahaEntryType type() {
		return KahaEntryType.KAHA_SUBSCRIPTION_COMMAND;
	}
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != KahaSubscriptionCommand.class)) {
			return false;
		}
		return equals((KahaSubscriptionCommand) obj);
	}
	public boolean equals(KahaSubscriptionCommand obj) {
		if ((hasDestination() ^ obj.hasDestination()))
			return false;
		if ((hasDestination()) && (!getDestination().equals(obj.getDestination())))
			return false;
		if ((hasSubscriptionKey() ^ obj.hasSubscriptionKey()))
			return false;
		if ((hasSubscriptionKey()) && (!getSubscriptionKey().equals(obj.getSubscriptionKey())))
			return false;
		if ((hasRetroactive() ^ obj.hasRetroactive()))
			return false;
		if ((hasRetroactive()) && (getRetroactive() != obj.getRetroactive()))
			return false;
		if ((hasSubscriptionInfo() ^ obj.hasSubscriptionInfo()))
			return false;
		if ((hasSubscriptionInfo()) && (!getSubscriptionInfo().equals(obj.getSubscriptionInfo())))
			return false;
		return true;
	}
	public int hashCode() {
		int rc = 172060159;
		if (hasDestination()) {
			rc ^= 0xE2FEBEE ^ getDestination().hashCode();
		}
		if (hasSubscriptionKey()) {
			rc ^= 0x710013E2 ^ getSubscriptionKey().hashCode();
		}
		if (hasRetroactive()) {
			rc ^= 0x1E865B04 ^ (getRetroactive() ? 3 : -3);
		}
		if (hasSubscriptionInfo()) {
			rc ^= 0xAF019F8B ^ getSubscriptionInfo().hashCode();
		}
		return rc;
	}
}
