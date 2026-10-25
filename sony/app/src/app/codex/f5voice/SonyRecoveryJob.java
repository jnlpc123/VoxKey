package app.codex.f5voice;
import android.app.job.*;
public final class SonyRecoveryJob extends JobService {
 @Override public boolean onStartJob(JobParameters p){SonyRecovery.start(this);return false;}
 @Override public boolean onStopJob(JobParameters p){return SonyRecovery.enabled(this);}
}
