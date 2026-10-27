package app.codex.f5voice.compat.com.android.integrated;
import android.app.job.*;
public final class RecoveryJob extends JobService {
 @Override public boolean onStartJob(JobParameters p){RecoveryState.start(this);return false;}
 @Override public boolean onStopJob(JobParameters p){return RecoveryState.enabled(this);}
}
