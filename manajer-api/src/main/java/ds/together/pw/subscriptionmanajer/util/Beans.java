package ds.together.pw.subscriptionmanajer.util;

import ds.together.pw.subscriptionmanajer.exception.BeanCopyException;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author x
 * @version 1.0
 * @since 2023/7/16 21:33
 */
public class Beans {


    /**
     * Copy the elements of a list to a new list of a different type.
     *
     * @param list        The list to copy from.
     * @param targetClass The class of the elements in the new list.
     * @param <T>         The type of the elements in the new list.
     * @return A new list containing the copied elements.
     * @throws BeanCopyException if there is an error copying the elements.
     */
    public static <T> List<T> copyList(List<?> list, Class<T> targetClass) {
        return list.stream().map(obj -> {
            T t;
            try {
                t = targetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new BeanCopyException("bean copy error, class: " + targetClass.getName() + ", e:", e);
            }
            BeanUtils.copyProperties(obj, t);
            return t;
        }).collect(Collectors.toList());
    }
}
