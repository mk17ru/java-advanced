package info.kgeorgiy.ja.kozlov.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedQuery {

    private final String EMPTY_STRING = "";

    private <R> Stream<Map.Entry<R, List<Student>>> getGroup(Collection<Student> students, Function<Student, R> param) {
        return getEntryStream(getSortedStream(students).collect(Collectors.groupingBy(param)));
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return getLargestGroupBy(students, gr -> gr.getValue().stream().map(Student::getGroup).distinct().count(),
                Student::getFirstName, EMPTY_STRING, Map.Entry.comparingByKey());
    }

    private <R> Stream<R> getSortedStream(Collection<R> students) {
        return students.stream().sorted();
    }

    private <T, A> Stream<Map.Entry<T, Long>> groupingByCount(Collection<Student> students, Function<Student, T> param,
                                                              Comparator<A> firstComparator) {
        return getEntryStream(getSortedStream(students).collect(Collectors.groupingBy(param, Collectors.counting())));
    }

    private <T, R> Stream<Map.Entry<T, R>> getEntryStream(Map<T, R> collect) {
        return collect.entrySet().stream();
    }


    private <T> Stream<Map.Entry<T, List<Student>>> groupingByList(Collection<Student> students, Function<Student, T> param) {
        return getEntryStream(getSortedStream(students).collect(Collectors.groupingBy(param)));
    }

    private <R> R getLargestGroupBy(Collection<Student> students,
                                    ToLongFunction<Map.Entry<R, List<Student>>> firstComparator,
                                    Function<Student, R> func, R defaultValue,
                                    Comparator<Map.Entry<R, List<Student>>> secondComparator) {
        return getGroup(students, func).
                max(Comparator.comparingLong(firstComparator).thenComparing(secondComparator))
                .map(Map.Entry::getKey).orElse(defaultValue);
    }

    private <R> List<R> getSomethingByInd(int[] inds, Collection<Student> students, Function<Student, R> func) {
        return collectList(Arrays.stream(inds).mapToObj(List.copyOf(students)::get).map(func));
    }

    private List<Group> getGroupBy(Collection<Student> st, Function<Collection<Student>, List<Student>> sortFunc) {
        return collectList(getGroup(st, Student::getGroup).sorted(Map.Entry.comparingByKey())
                .map(group -> new Group(group.getKey(), sortFunc.apply(group.getValue()))));
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getSomethingByInd(indices, students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getSomethingByInd(indices, students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getSomethingByInd(indices, students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getSomethingByInd(indices, students, StudentDB::getStudentFullName);
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupBy(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupBy(students, this::sortStudentsById);
    }

    private static <T, R extends Comparable<? super R>> Comparator<T> getReverseComparator(Function<T, R> func) {
        return Collections.reverseOrder(Comparator.comparing(func));
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students, gr -> gr.getValue().size(),
                Student::getGroup, null, Map.Entry.comparingByKey());
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students, gr -> getDistinctFirstNames(gr.getValue()).size(),
                Student::getGroup, null, Collections.reverseOrder(Map.Entry.comparingByKey()));
    }


    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getFirstNames(students).stream().collect(Collectors.toSet());
    }

    private <S, R> Stream<R> filterCollection(Collection<R> collection,
                                              Function<R, S> method, S element) {
        return getStream(collection).filter(student -> method.apply(student).equals(element));
    }

    private <T> List<Student> findListBy(Collection<Student> collection,
                                         Function<Student, T> method, T element) {
        return collectList(filterCollection(collection, method, element)
                .sorted(COMPARATOR_BY_FULL_NAMES));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findListBy(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findListBy(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findListBy(students, Student::getGroup, group);
    }


    public <T extends Comparable<T> > Map<T, T> findSomethingByGroup(Collection<Student> students, GroupName group,
                                                                     Function<Student, T> f1, Function<Student, T> f2) {
        return filterCollection(students, Student::getGroup, group).collect(Collectors.toMap(f1, f2,
                BinaryOperator.minBy(T::compareTo)
        ));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findSomethingByGroup(students, group, Student::getLastName, Student::getFirstName);
    }


    private static String getStudentFullName(Student student) {
        return String.join(" ", student.getFirstName(), student.getLastName());
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getSomethingFromList(students, Student::getGroup);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getSomethingFromList(students, Student::getFirstName);
    }

    private final static Comparator<Student> COMPARATOR_BY_FULL_NAMES =
            getReverseComparator(Student::getLastName)
                    .thenComparing(getReverseComparator(Student::getFirstName))
                    .thenComparing(Student::getId);


    private <T> List<T> sortSomethingBy(Collection<T> students, Comparator<T> comparator) {
        return collectList(getStream(students).sorted(comparator));
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortSomethingBy(students, Comparator.comparing(Student::getId));
    }


    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortSomethingBy(students, COMPARATOR_BY_FULL_NAMES);
    }

    private <T> List<T> getSomethingFromList(List<Student> list, Function<Student, T> method) {
        return collectList(getStream(list).map(method));
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getSomethingFromList(students, StudentDB::getStudentFullName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getSomethingFromList(students, Student::getLastName);
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return getStream(students).max(Comparator.comparing(Student::getId))
                .map(Student::getFirstName).orElse(EMPTY_STRING);
    }

    private <R> Stream<R> getStream(Collection<R> students) {
        return students.stream();
    }


    private <R> List<R> collectList(Stream<R> stream) {
        return stream.collect(Collectors.toList());
    }
}
