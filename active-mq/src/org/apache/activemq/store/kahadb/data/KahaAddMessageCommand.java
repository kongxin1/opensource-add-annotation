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

public final class KahaAddMessageCommand extends KahaAddMessageCommandBase<KahaAddMessageCommand> implements
		JournalCommand<KahaAddMessageCommand> {
	public ArrayList<String> missingFields() {
		ArrayList missingFields = super.missingFields();
		if (!hasDestination()) {
			missingFields.add("destination");
		}
		if (!hasMessageId()) {
			missingFields.add("messageId");
		}
		if (!hasMessage()) {
			missingFields.add("message");
		}
		if (hasTransactionInfo()) {
			try {
				getTransactionInfo().assertInitialized();
			} catch (UninitializedMessageException e) {
				missingFields.addAll(prefix(e.getMissingFields(), "transaction_info."));
			}
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
		clearTransactionInfo();
		clearDestination();
		clearMessageId();
		clearMessage();
		clearPriority();
		clearPrioritySupported();
	}
	public KahaAddMessageCommand clone() {
		return new KahaAddMessageCommand().mergeFrom(this);
	}
	public KahaAddMessageCommand mergeFrom(KahaAddMessageCommand other) {
		if (other.hasTransactionInfo()) {
			if (hasTransactionInfo())
				getTransactionInfo().mergeFrom(other.getTransactionInfo());
			else {
				setTransactionInfo(other.getTransactionInfo().clone());
			}
		}
		if (other.hasDestination()) {
			if (hasDestination())
				getDestination().mergeFrom(other.getDestination());
			else {
				setDestination(other.getDestination().clone());
			}
		}
		if (other.hasMessageId()) {
			setMessageId(other.getMessageId());
		}
		if (other.hasMessage()) {
			setMessage(other.getMessage());
		}
		if (other.hasPriority()) {
			setPriority(other.getPriority());
		}
		if (other.hasPrioritySupported()) {
			setPrioritySupported(other.getPrioritySupported());
		}
		return this;
	}
	public int serializedSizeUnframed() {
		if (this.memoizedSerializedSize != -1) {
			return this.memoizedSerializedSize;
		}
		int size = 0;
		if (hasTransactionInfo()) {
			size += computeMessageSize(1, getTransactionInfo());
		}
		if (hasDestination()) {
			size += computeMessageSize(2, getDestination());
		}
		if (hasMessageId()) {
			size += CodedOutputStream.computeStringSize(3, getMessageId());
		}
		if (hasMessage()) {
			size += CodedOutputStream.computeBytesSize(4, getMessage());
		}
		if (hasPriority()) {
			size += CodedOutputStream.computeInt32Size(5, getPriority());
		}
		if (hasPrioritySupported()) {
			size += CodedOutputStream.computeBoolSize(6, getPrioritySupported());
		}
		this.memoizedSerializedSize = size;
		return size;
	}
	public KahaAddMessageCommand mergeUnframed(CodedInputStream input) throws IOException {
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
					if (hasTransactionInfo())
						getTransactionInfo().mergeFramed(input);
					else {
						setTransactionInfo((KahaTransactionInfo) new KahaTransactionInfo().mergeFramed(input));
					}
					break;
				case 18:
					if (hasDestination())
						getDestination().mergeFramed(input);
					else {
						setDestination((KahaDestination) new KahaDestination().mergeFramed(input));
					}
					break;
				case 26:
					setMessageId(input.readString());
					break;
				case 34:
					setMessage(input.readBytes());
					break;
				case 40:
					setPriority(input.readInt32());
					break;
				case 48:
					setPrioritySupported(input.readBool());
			}
		}
	}
	public void writeUnframed(CodedOutputStream output) throws IOException {
		if (hasTransactionInfo()) {
			writeMessage(output, 1, getTransactionInfo());
		}
		if (hasDestination()) {
			writeMessage(output, 2, getDestination());
		}
		if (hasMessageId()) {
			output.writeString(3, getMessageId());
		}
		if (hasMessage()) {
			output.writeBytes(4, getMessage());
		}
		if (hasPriority()) {
			output.writeInt32(5, getPriority());
		}
		if (hasPrioritySupported())
			output.writeBool(6, getPrioritySupported());
	}
	public static KahaAddMessageCommand parseUnframed(CodedInputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaAddMessageCommand) new KahaAddMessageCommand().mergeUnframed(data).checktInitialized();
	}
	public static KahaAddMessageCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaAddMessageCommand) ((KahaAddMessageCommand) new KahaAddMessageCommand().mergeUnframed(data))
				.checktInitialized();
	}
	public static KahaAddMessageCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaAddMessageCommand) ((KahaAddMessageCommand) new KahaAddMessageCommand().mergeUnframed(data))
				.checktInitialized();
	}
	public static KahaAddMessageCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaAddMessageCommand) ((KahaAddMessageCommand) new KahaAddMessageCommand().mergeUnframed(data))
				.checktInitialized();
	}
	public static KahaAddMessageCommand parseFramed(CodedInputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaAddMessageCommand) ((KahaAddMessageCommand) new KahaAddMessageCommand().mergeFramed(data))
				.checktInitialized();
	}
	public static KahaAddMessageCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaAddMessageCommand) ((KahaAddMessageCommand) new KahaAddMessageCommand().mergeFramed(data))
				.checktInitialized();
	}
	public static KahaAddMessageCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaAddMessageCommand) ((KahaAddMessageCommand) new KahaAddMessageCommand().mergeFramed(data))
				.checktInitialized();
	}
	public static KahaAddMessageCommand parseFramed(InputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaAddMessageCommand) ((KahaAddMessageCommand) new KahaAddMessageCommand().mergeFramed(data))
				.checktInitialized();
	}
	public String toString() {
		return toString(new StringBuilder(), "").toString();
	}
	public StringBuilder toString(StringBuilder sb, String prefix) {
		if (hasTransactionInfo()) {
			sb.append(prefix + "transaction_info {\n");
			getTransactionInfo().toString(sb, prefix + "  ");
			sb.append(prefix + "}\n");
		}
		if (hasDestination()) {
			sb.append(prefix + "destination {\n");
			getDestination().toString(sb, prefix + "  ");
			sb.append(prefix + "}\n");
		}
		if (hasMessageId()) {
			sb.append(prefix + "messageId: ");
			sb.append(getMessageId());
			sb.append("\n");
		}
		if (hasMessage()) {
			sb.append(prefix + "message: ");
			sb.append(getMessage());
			sb.append("\n");
		}
		if (hasPriority()) {
			sb.append(prefix + "priority: ");
			sb.append(getPriority());
			sb.append("\n");
		}
		if (hasPrioritySupported()) {
			sb.append(prefix + "prioritySupported: ");
			sb.append(getPrioritySupported());
			sb.append("\n");
		}
		return sb;
	}
	public void visit(Visitor visitor) throws IOException {
		visitor.visit(this);
	}
	public KahaEntryType type() {
		return KahaEntryType.KAHA_ADD_MESSAGE_COMMAND;
	}
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != KahaAddMessageCommand.class)) {
			return false;
		}
		return equals((KahaAddMessageCommand) obj);
	}
	public boolean equals(KahaAddMessageCommand obj) {
		if ((hasTransactionInfo() ^ obj.hasTransactionInfo()))
			return false;
		if ((hasTransactionInfo()) && (!getTransactionInfo().equals(obj.getTransactionInfo())))
			return false;
		if ((hasDestination() ^ obj.hasDestination()))
			return false;
		if ((hasDestination()) && (!getDestination().equals(obj.getDestination())))
			return false;
		if ((hasMessageId() ^ obj.hasMessageId()))
			return false;
		if ((hasMessageId()) && (!getMessageId().equals(obj.getMessageId())))
			return false;
		if ((hasMessage() ^ obj.hasMessage()))
			return false;
		if ((hasMessage()) && (!getMessage().equals(obj.getMessage())))
			return false;
		if ((hasPriority() ^ obj.hasPriority()))
			return false;
		if ((hasPriority()) && (getPriority() != obj.getPriority()))
			return false;
		if ((hasPrioritySupported() ^ obj.hasPrioritySupported()))
			return false;
		if ((hasPrioritySupported()) && (getPrioritySupported() != obj.getPrioritySupported()))
			return false;
		return true;
	}
	public int hashCode() {
		int rc = 1601475350;
		if (hasTransactionInfo()) {
			rc ^= 0xFD5C48C ^ getTransactionInfo().hashCode();
		}
		if (hasDestination()) {
			rc ^= 0xE2FEBEE ^ getDestination().hashCode();
		}
		if (hasMessageId()) {
			rc ^= 0x219D4362 ^ getMessageId().hashCode();
		}
		if (hasMessage()) {
			rc ^= 0x9C2397E7 ^ getMessage().hashCode();
		}
		if (hasPriority()) {
			rc ^= 0xBE62DDC4 ^ getPriority();
		}
		if (hasPrioritySupported()) {
			rc ^= 0x3504534A ^ (getPrioritySupported() ? 6 : -6);
		}
		return rc;
	}
}
