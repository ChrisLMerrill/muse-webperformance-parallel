package com.webperformance.muse.parallel;

import org.musetest.core.*;
import org.musetest.core.execution.*;
import org.musetest.core.resultstorage.*;
import org.musetest.core.suite.*;
import org.musetest.core.test.*;
import org.musetest.core.variables.*;
import org.slf4j.*;

import java.util.*;

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
public class ParallelTestSuiteRunner implements MuseTestSuiteRunner
	{
	@Override
	public MuseTestSuiteResult execute(MuseProject project, MuseTestSuite suite)
		{
		_result = new BaseMuseTestSuiteResult(suite);

		// Put all the tests in a queue (ConcurrentQueue is overkill, since in a synchronized block)
		Iterator<TestConfiguration> tests = suite.getTests(project);

		// wait for them to finish
		synchronized (this)
			{
			// while tests are not all started and complete
			while (tests.hasNext() || _started > _completed)
				{
				// while there are more tests to start and there is room to run them (without exceeding max_concurrency)
				while (tests.hasNext() && _running < _max_concurrency)
					{
					TestConfiguration configuration = tests.next();
					TestRunner runner = new NotifyingTestRunner(project, configuration);
					if (_output != null)
				        runner.getExecutionContext().setVariable(SaveTestResultsToDisk.OUTPUT_FOLDER_VARIABLE_NAME, _output.getOutputFolderName(configuration), VariableScope.Execution);
					runner.runTest();
					_running++;
					_started++;
					}

				try
					{
					wait(5000); // don't hang if something goes wrong
					}
				catch (InterruptedException e)
					{
					LOG.error("test suite was interrupted");
					return _result;
					}
				}
			return _result;
			}
		}

	@Override
	public void setOutputPath(String path)
		{
		_output = new TestSuiteOutputOnDisk(path);
		}

	private synchronized void notifyComplete(MuseTestResult result)
		{
		_running--;
		_completed++;
		_result.addTestResult(result);
		notify();
		}

	public long getMaxConcurrency()
		{
		return _max_concurrency;
		}

	public void setMaxConcurrency(long max_concurrency)
		{
		_max_concurrency = max_concurrency;
		}

	private BaseMuseTestSuiteResult _result;
	private TestSuiteOutputOnDisk _output = null;
	private long _max_concurrency = DEFAULT_MAX_CONCURRENCY;
	private int _running = 0;
	private int _completed = 0;
	private int _started = 0;

	public final static Long DEFAULT_MAX_CONCURRENCY = 10L;

	class NotifyingTestRunner extends ThreadedTestRunner
		{
		NotifyingTestRunner(MuseProject project, TestConfiguration test)
			{
			super(project, test);
			}

		@Override
		public void run()
			{
			super.run();
			notifyComplete(getResult());
			}
		}

	private final static Logger LOG = LoggerFactory.getLogger(ParallelTestSuiteRunner.class);
	}