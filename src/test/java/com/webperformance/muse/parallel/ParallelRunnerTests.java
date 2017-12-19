package com.webperformance.muse.parallel;

import org.junit.*;
import org.musetest.core.*;
import org.musetest.core.context.*;
import org.musetest.core.mocks.*;
import org.musetest.core.project.*;
import org.musetest.core.suite.*;
import org.musetest.core.values.*;

import java.text.*;

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
public class ParallelRunnerTests
	{
	@Test
	public void runInParallel()
		{
		ParallelTestSuiteRunner runner = new ParallelTestSuiteRunner();
		runner.execute(_project, _suite);
		long duration = run(runner);

		// verify they all ran in roughly the test duration (e.g. all in parallel)
		Assert.assertTrue(80 < duration && duration < 120);
		}

	@Test
	public void runInParallelWithConcurrencyLimit() throws MuseExecutionError
		{
		ParallelTestSuiteRunnerConfiguration config = new ParallelTestSuiteRunnerConfiguration();
		config.parameters().addSource(ParallelTestSuiteRunnerConfiguration.MAX_CONCURRENCY_PARAM_NAME, ValueSourceConfiguration.forValue(1));
		ParallelTestSuiteRunner runner = config.createRunner(new BaseExecutionContext(_project));
		long start_time = System.currentTimeMillis();
		run(runner);
		long duration = System.currentTimeMillis() - start_time;

		// verify that it took roughly 4x the test duration, because only 1 ran at a time.
		Assert.assertTrue(350 < duration && duration < 450);
		}

	private long run(ParallelTestSuiteRunner runner)
		{
		long start_time = System.currentTimeMillis();
		runner.execute(_project, _suite);
		return System.currentTimeMillis() - start_time;
		}

	@Test
	public void setConcurrencyFromConfig() throws MuseExecutionError
		{
		ParallelTestSuiteRunnerConfiguration config = new ParallelTestSuiteRunnerConfiguration();
		MuseExecutionContext context = new BaseExecutionContext(new SimpleProject());
		ParallelTestSuiteRunner runner = config.createRunner(context);
		Assert.assertEquals(ParallelTestSuiteRunner.DEFAULT_MAX_CONCURRENCY.longValue(), runner.getMaxConcurrency());

		config.parameters().addSource(ParallelTestSuiteRunnerConfiguration.MAX_CONCURRENCY_PARAM_NAME, ValueSourceConfiguration.forValue(3));
		runner = config.createRunner(context);
		Assert.assertEquals(3, runner.getMaxConcurrency());
		}

	static SimpleDateFormat formatter = new SimpleDateFormat("mm:ss:SSS");

	class SlowMockTest extends MockTest
		{
		@Override
		protected MuseTestResult executeImplementation(TestExecutionContext context)
			{
			try
				{
				Thread.sleep(100);
				}
			catch (InterruptedException e)
				{ /* noop */ }
			return super.executeImplementation(context);
			}
		}

	@Before
	public void setup()
		{
		_project = new SimpleProject();

		_suite = new SimpleTestSuite();
		_suite.add(new SlowMockTest());
		_suite.add(new SlowMockTest());
		_suite.add(new SlowMockTest());
		_suite.add(new SlowMockTest());
		}

	private SimpleProject _project;
	private SimpleTestSuite _suite;
	}


