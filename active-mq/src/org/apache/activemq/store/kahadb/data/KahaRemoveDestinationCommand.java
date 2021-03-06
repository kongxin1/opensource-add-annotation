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

public final class KahaRemoveDestinationCommand extends KahaRemoveDestinationCommandBase<KahaRemoveDestinationCommand>
		implements JournalCommand<KahaRemoveDestinationCommand> {
	public ArrayList<String> missingFields() {
		ArrayList missingFields = super.missingFields();
		if (!hasDestination()) {
			missingFields.add("destination");
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
	}
	public KahaRemoveDestinationCommand clone() {
		return new KahaRemoveDestinationCommand().mergeFrom(this);
	}
	public KahaRemoveDestinationCommand mergeFrom(KahaRemoveDestinationCommand other) {
		if (other.hasDestination()) {
			if (hasDestination())
				getDestination().mergeFrom(other.getDestination());
			else {
				setDestination(other.getDestination().clone());
			}
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
		this.memoizedSerializedSize = size;
		return size;
	}
	public KahaRemoveDestinationCommand mergeUnframed(CodedInputStream input) throws IOException {
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
					else
						setDestination((KahaDestination) new KahaDestination().mergeFramed(input));
					break;
			}
		}
	}
	public void writeUnframed(CodedOutputStream output) throws IOException {
		if (hasDestination())
			writeMessage(output, 1, getDestination());
	}
	public static KahaRemoveDestinationCommand parseUnframed(CodedInputStream data)
			throws InvalidProtocolBufferException, IOException {
		return (KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand().mergeUnframed(data)
				.checktInitialized();
	}
	public static KahaRemoveDestinationCommand parseUnframed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaRemoveDestinationCommand) ((KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand()
				.mergeUnframed(data)).checktInitialized();
	}
	public static KahaRemoveDestinationCommand parseUnframed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaRemoveDestinationCommand) ((KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand()
				.mergeUnframed(data)).checktInitialized();
	}
	public static KahaRemoveDestinationCommand parseUnframed(InputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaRemoveDestinationCommand) ((KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand()
				.mergeUnframed(data)).checktInitialized();
	}
	public static KahaRemoveDestinationCommand parseFramed(CodedInputStream data)
			throws InvalidProtocolBufferException, IOException {
		return (KahaRemoveDestinationCommand) ((KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand()
				.mergeFramed(data)).checktInitialized();
	}
	public static KahaRemoveDestinationCommand parseFramed(Buffer data) throws InvalidProtocolBufferException {
		return (KahaRemoveDestinationCommand) ((KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand()
				.mergeFramed(data)).checktInitialized();
	}
	public static KahaRemoveDestinationCommand parseFramed(byte[] data) throws InvalidProtocolBufferException {
		return (KahaRemoveDestinationCommand) ((KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand()
				.mergeFramed(data)).checktInitialized();
	}
	public static KahaRemoveDestinationCommand parseFramed(InputStream data) throws InvalidProtocolBufferException,
			IOException {
		return (KahaRemoveDestinationCommand) ((KahaRemoveDestinationCommand) new KahaRemoveDestinationCommand()
				.mergeFramed(data)).checktInitialized();
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
		return sb;
	}
	public void visit(Visitor visitor) throws IOException {
		visitor.visit(this);
	}
	public KahaEntryType type() {
		return KahaEntryType.KAHA_REMOVE_DESTINATION_COMMAND;
	}
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != KahaRemoveDestinationCommand.class)) {
			return false;
		}
		return equals((KahaRemoveDestinationCommand) obj);
	}
	public boolean equals(KahaRemoveDestinationCommand obj) {
		if ((hasDestination() ^ obj.hasDestination()))
			return false;
		if ((hasDestination()) && (!getDestination().equals(obj.getDestination())))
			return false;
		return true;
	}
	public int hashCode() {
		int rc = 302570256;
		if (hasDestination()) {
			rc ^= 0xE2FEBEE ^ getDestination().hashCode();
		}
		return rc;
	}
}
