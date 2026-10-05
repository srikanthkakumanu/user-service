package com.users.domain.model;

import java.util.List;

/** One page of results with the total number of matches. */
public record PageResult<T>(List<T> items, long total, int page, int size) {

	public PageResult {
		items = List.copyOf(items);
	}

	public <R> PageResult<R> map(java.util.function.Function<T, R> mapper) {
		return new PageResult<>(items.stream().map(mapper).toList(), total, page, size);
	}
}
