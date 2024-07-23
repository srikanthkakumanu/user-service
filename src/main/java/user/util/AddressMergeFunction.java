package user.util;

@FunctionalInterface
public interface AddressMergeFunction<T, D, M, R> {
    R apply(T t, D d, M m, R r);
}


