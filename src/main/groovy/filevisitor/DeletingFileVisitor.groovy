package filevisitor

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class DeletingFileVisitor extends SimpleFileVisitor<Path> {

    static Logger logger = LoggerFactory.getLogger(DeletingFileVisitor.class);

    @Override
    FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
        logger.debug("deleting file : ${path.getFileName()}")
        Files.delete(path)
        return checkNotExist(path)
    }

    @Override
    FileVisitResult postVisitDirectory(Path path, IOException exception) throws IOException {
        if (exception == null) {
            logger.debug("deleting directory : ${path.getFileName()}")
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
