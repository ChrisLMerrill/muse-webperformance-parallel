package com.webperformance.muse.parallel;

import org.musetest.core.*;
import org.musetest.core.resource.types.*;
import org.musetest.core.suite.*;
import org.musetest.core.values.*;

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
@MuseTypeId("parallel-test-suite-runner")
public class ParallelTestSuiteRunnerConfiguration extends SuiteRunnerConfiguration
	{
	@Override
	public ResourceType getType()
		{
		return TYPE;
		}

	@Override
	public ParallelTestSuiteRunner createRunner(MuseExecutionContext context) throws MuseExecutionError
		{
		final ParallelTestSuiteRunner runner = new ParallelTestSuiteRunner();

		MuseValueSource concurrency_source = BaseValueSource.getValueSource(parameters(), MAX_CONCURRENCY_PARAM_NAME, false, context.getProject());
		if (concurrency_source != null)
			{
			Long value = BaseValueSource.getValue(concurrency_source, context, false, Long.class);
			runner.setMaxConcurrency(value);
			}

		return runner;
		}

	public final static String TYPE_ID = ParallelTestSuiteRunnerConfiguration.class.getAnnotation(MuseTypeId.class).value();
	public final static ResourceType TYPE = new ParallelTestSuiteRunnerResourceType();

	public final static String MAX_CONCURRENCY_PARAM_NAME = "max-concurrency";

	public static class ParallelTestSuiteRunnerResourceType extends ResourceType
		{
		public ParallelTestSuiteRunnerResourceType()
			{
			super(TYPE_ID, "Parallel Test Suite Runner", ParallelTestSuiteRunnerConfiguration.class);
			}
		}
	}


