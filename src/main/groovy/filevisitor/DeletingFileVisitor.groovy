package filevisitor

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class DeletingFileVisitor extends SimpleFileVisitor<Path> {

    @Override
    FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
        System.out.println("deleting file : ${path.getFileName()}")
        Files.delete(path)
        return checkNotExist(path)
    }

    @Override
    FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
        if (exception == null) {
            System.out.println("deleting directory : ${path.getFileName()}")
            Files.delete(path)
            return checkNotExist(path)
        } else {
            throw exception
        }
    }

    private static FileVisitResult checkNotExist(final Path path) throws IOException {
        boolean deleted = Files.deleteIfExists(path)
        if (!deleted) {
            return FileVisitResult.CONTINUE
        } else {
            throw new IOException()
        }
    }

}
