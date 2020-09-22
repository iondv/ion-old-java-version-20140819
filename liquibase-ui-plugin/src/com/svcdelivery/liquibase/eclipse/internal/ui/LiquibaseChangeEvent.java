/**
 * Copyright 2012 Nick Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.svcdelivery.liquibase.eclipse.internal.ui;

/**
 * @author nick
 */
public class LiquibaseChangeEvent {
	/**
	 * The change event type.
	 */
	private final LiquibaseChangeEventType type;
	/**
	 * The results.
	 */
	private final LiquibaseResult result;

	/**
	 * @param eventType
	 *            The change event type.
	 * @param liquibaseResult
	 *            The results.
	 */
	public LiquibaseChangeEvent(final LiquibaseChangeEventType eventType,
			final LiquibaseResult liquibaseResult) {
		type = eventType;
		result = liquibaseResult;
	}

	/**
	 * @return The change event type.
	 */
	public final LiquibaseChangeEventType getType() {
		return type;
	}

	/**
	 * @return The results.
	 */
	public final LiquibaseResult getResult() {
		return result;
	}

}
