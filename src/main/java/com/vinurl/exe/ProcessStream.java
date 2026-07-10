package com.vinurl.exe;

import org.apache.commons.exec.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

import static com.vinurl.exe.Executable.killProcess;
import static com.vinurl.exe.Executable.registerProcess;

public class ProcessStream {
	private final String id;
	private final String[] arguments;
	private final SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
	private final ConcurrentHashMap<String, Flow.Subscription> subscriptions = new ConcurrentHashMap<>();
	private Process process;

	public ProcessStream(String id, CommandLine command) {
		this.id = id;
		this.arguments = command.toStrings();
		if (registerProcess(id, this)) {
			CompletableFuture.runAsync(this::startProcess);
		}
	}

	public String getId() {
		return id;
	}

	public Process getProcess() {
		return process;
	}

	public SubscriberBuilder subscribe(String subscriberId) {
		return new SubscriberBuilder(subscriberId);
	}

	public void unsubscribe(String subscriberId) {
		Flow.Subscription subscription = subscriptions.remove(subscriberId);
		if (subscription != null) {
			subscription.cancel();
		}
	}

	public int subscriberCount() {
		return subscriptions.size();
	}

	public void onExit(Runnable callback) {
		if (process != null) {
			process.onExit().thenRun(() -> {
				subscriptions.keySet().forEach(this::unsubscribe);
				callback.run();
			});
		}
	}

	private void startProcess() {
		try {
			process = new ProcessBuilder()
				.command(arguments)
				.redirectErrorStream(true)
				.start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null && !publisher.isClosed()) {
					publisher.submit(line);
				}
			}

			int exitCode = process.waitFor();

			if (exitCode == 0) {
				publisher.close();
			} else {
				publisher.closeExceptionally(new IOException("Process failed with code: " + exitCode));
			}
		} catch (IOException | InterruptedException e) {
			publisher.closeExceptionally(e);
		} finally {
			killProcess(id);
		}
	}

	public class SubscriberBuilder {
		private final String subscriberId;
		private Consumer<String> onOutput = (s) -> {};
		private Consumer<Throwable> onError = (t) -> {};
		private Runnable onComplete = () -> {};

		public SubscriberBuilder(String subscriberId) {
			this.subscriberId = subscriberId;
		}

		public SubscriberBuilder onOutput(Consumer<String> consumer) {
			this.onOutput = consumer;
			return this;
		}

		public SubscriberBuilder onError(Consumer<Throwable> consumer) {
			this.onError = consumer;
			return this;
		}

		public SubscriberBuilder onComplete(Runnable runnable) {
			this.onComplete = runnable;
			return this;
		}

		public void start() {
			publisher.subscribe(new Flow.Subscriber<>() {
				@Override
				public void onSubscribe(Flow.Subscription subscription) {
					subscriptions.put(subscriberId, subscription);
					subscription.request(Long.MAX_VALUE);
				}

				@Override
				public void onNext(String item) {
					onOutput.accept(item);
				}

				@Override
				public void onError(Throwable throwable) {
					subscriptions.remove(subscriberId);
					onError.accept(throwable);
				}

				@Override
				public void onComplete() {
					subscriptions.remove(subscriberId);
					onComplete.run();
				}
			});
		}
	}
}
