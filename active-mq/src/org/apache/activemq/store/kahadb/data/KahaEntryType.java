package org.apache.activemq.store.kahadb.data;

import org.apache.activemq.protobuf.Message;

public enum KahaEntryType {
	KAHA_TRACE_COMMAND("KAHA_TRACE_COMMAND", 0), KAHA_ADD_MESSAGE_COMMAND("KAHA_ADD_MESSAGE_COMMAND", 1),
	KAHA_REMOVE_MESSAGE_COMMAND("KAHA_REMOVE_MESSAGE_COMMAND", 2), KAHA_PREPARE_COMMAND("KAHA_PREPARE_COMMAND", 3),
	KAHA_COMMIT_COMMAND("KAHA_COMMIT_COMMAND", 4), KAHA_ROLLBACK_COMMAND("KAHA_ROLLBACK_COMMAND", 5),
	KAHA_REMOVE_DESTINATION_COMMAND("KAHA_REMOVE_DESTINATION_COMMAND", 6), KAHA_SUBSCRIPTION_COMMAND(
			"KAHA_SUBSCRIPTION_COMMAND", 7), KAHA_PRODUCER_AUDIT_COMMAND("KAHA_PRODUCER_AUDIT_COMMAND", 8),
	KAHA_ACK_MESSAGE_FILE_MAP_COMMAND("KAHA_ACK_MESSAGE_FILE_MAP_COMMAND", 9), KAHA_UPDATE_MESSAGE_COMMAND(
			"KAHA_UPDATE_MESSAGE_COMMAND", 10), KAHA_ADD_SCHEDULED_JOB_COMMAND("KAHA_ADD_SCHEDULED_JOB_COMMAND", 11),
	KAHA_RESCHEDULE_JOB_COMMAND("KAHA_RESCHEDULE_JOB_COMMAND", 12), KAHA_REMOVE_SCHEDULED_JOB_COMMAND(
			"KAHA_REMOVE_SCHEDULED_JOB_COMMAND", 13), KAHA_REMOVE_SCHEDULED_JOBS_COMMAND(
			"KAHA_REMOVE_SCHEDULED_JOBS_COMMAND", 14), KAHA_DESTROY_SCHEDULER_COMMAND("KAHA_DESTROY_SCHEDULER_COMMAND",
			15);
	private final String name;
	private final int value;

	private KahaEntryType(String name, int value) {
		this.name = name;
		this.value = value;
	}
	public final int getNumber() {
		return this.value;
	}
	public final String toString() {
		return this.name;
	}
	public static KahaEntryType valueOf(int value) {
		switch (value) {
			case 0:
				return KAHA_TRACE_COMMAND;
			case 1:
				return KAHA_ADD_MESSAGE_COMMAND;
			case 2:
				return KAHA_REMOVE_MESSAGE_COMMAND;
			case 3:
				return KAHA_PREPARE_COMMAND;
			case 4:
				return KAHA_COMMIT_COMMAND;
			case 5:
				return KAHA_ROLLBACK_COMMAND;
			case 6:
				return KAHA_REMOVE_DESTINATION_COMMAND;
			case 7:
				return KAHA_SUBSCRIPTION_COMMAND;
			case 8:
				return KAHA_PRODUCER_AUDIT_COMMAND;
			case 9:
				return KAHA_ACK_MESSAGE_FILE_MAP_COMMAND;
			case 10:
				return KAHA_UPDATE_MESSAGE_COMMAND;
			case 11:
				return KAHA_ADD_SCHEDULED_JOB_COMMAND;
			case 12:
				return KAHA_RESCHEDULE_JOB_COMMAND;
			case 13:
				return KAHA_REMOVE_SCHEDULED_JOB_COMMAND;
			case 14:
				return KAHA_REMOVE_SCHEDULED_JOBS_COMMAND;
			case 15:
				return KAHA_DESTROY_SCHEDULER_COMMAND;
		}
		return null;
	}
	public Message createMessage() {
		switch (ordinal()) {
			case 1:
				return new KahaTraceCommand();
			case 2:
				return new KahaAddMessageCommand();
			case 3:
				return new KahaRemoveMessageCommand();
			case 4:
				return new KahaPrepareCommand();
			case 5:
				return new KahaCommitCommand();
			case 6:
				return new KahaRollbackCommand();
			case 7:
				return new KahaRemoveDestinationCommand();
			case 8:
				return new KahaSubscriptionCommand();
			case 9:
				return new KahaProducerAuditCommand();
			case 10:
				return new KahaAckMessageFileMapCommand();
			case 11:
				return new KahaUpdateMessageCommand();
			case 12:
				return new KahaAddScheduledJobCommand();
			case 13:
				return new KahaRescheduleJobCommand();
			case 14:
				return new KahaRemoveScheduledJobCommand();
			case 15:
				return new KahaRemoveScheduledJobsCommand();
			case 16:
				return new KahaDestroySchedulerCommand();
		}
		return null;
	}
}
