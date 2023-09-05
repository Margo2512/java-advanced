package info.kgeorgiy.ja.kadochnikova.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
public class StudentDB implements StudentQuery {
    private static final Comparator<Student> COMPARATOR = Comparator
            .comparing(Student::getLastName, Comparator.reverseOrder())
            .thenComparing(Student::getFirstName, Comparator.reverseOrder())
            .thenComparing(Student::getId);
    private List<String> get(List<Student> students, Function<? super Student,? extends String> mapper) {
        return students.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
    private List<Student> find(Collection<Student> students, Predicate<Student> pred) {
        return students.stream()
                .filter(pred)
                .sorted(COMPARATOR)
                .collect(Collectors.toList());
    }
    public List<String> getFirstNames(List<Student> students) {
        return get(students, Student::getFirstName);
    }
    public List<String> getLastNames(List<Student> students) {
        return get(students, Student::getLastName);
    }

    public List<GroupName> getGroups(List<Student> students) {
        return students.stream()
                .map(Student::getGroup)
                .collect(Collectors.toList());
    }
    public List<String> getFullNames(List<Student> students) {
        return get(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.comparing(Student::getId))
                .map(Student::getFirstName)
                .orElse("");
    }
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted(Comparator.comparing(Student::getId))
                .collect(Collectors.toList());
    }

    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream()
                .sorted(COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return find(students, student -> student.getFirstName().equals(name));
    }

    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return find(students, student -> student.getLastName().equals(name));
    }


    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return find(students, student -> student.getGroup().equals(group));
    }

    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (x, y) -> (x.compareTo(y) < 0) ? x : y));
    }
}