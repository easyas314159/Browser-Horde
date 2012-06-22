public abstract class DeferredAction {
	private final Collection<DeferredAction> onSuccess;
	private final Collection<DeferredAction> onFailure;

	protected DeferredAction() {
		onSuccess = new ArrayList<DeferredAction>();
		onFailure = new ArrayList<DeferredAction>();
	}

	public abstract void execute() throws Exception;

	public abstract int getMaxAttempts();

	public final Collection<DeferredAction> getSuccessActions() {
		return Collections.unmodifiableCollection(onSuccess);
	}
	public final Collection<DeferredAction> getFailureActions() {
		return Collections.unmodifiableCollection(onFailure);
	}
}
