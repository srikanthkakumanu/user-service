package user.util;

@FunctionalInterface
public interface AddressMergeFunc<T, D, M, R> {
    R apply(T t, D d, M m, R r);
}


