package filevisitor

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class PrintingFileVisitor implements FileVisitor<Path> {

    static Logger logger = LoggerFactory.getLogger(PrintingFileVisitor.class);

    private int indentSize

    @Override
    FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        logger.debug("preVisitDirectory : ${dir.getFileName()}")
        this.indentSize++
        return FileVisitResult.CONTINUE
    }

    @Override
    FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        logger.debug("visiteFile : ${file.getFileName()}")
        return FileVisitResult.CONTINUE
    }

    @Override
    FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        this.indentSize--
        logger.debug("postVisiteDirectory : ${dir.getFileName()}")
        return FileVisitResult.CONTINUE
    }

    @Override
    FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        String error = String.format(" [exception=%s, message=%s]",
            exc.getClass(), exc.getMessage())
        logger.debug("visitFileFailed : ${file.getFileName()}${error}")
        return FileVisitResult.CONTINUE
    }

    void print(String message) {
        String indent =
        logger.debug(this.getIndent(this.indentSize, '    ') + message)
    }

    String getIndent(int depth, String whitespace) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < depth; i++) {
            sb.append(whitespace)
        }
        return sb.toString()
    }
}
