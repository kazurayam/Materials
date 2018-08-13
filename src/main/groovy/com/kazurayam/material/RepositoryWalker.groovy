package com.kazurayam.material

class RepositoryWalker {

    static RepositoryRoot warkRepository(RepositoryRoot repoRoot, RepositoryVisitor visitor) {
        if (repoRoot == null) {
            throw new IllegalArgumentException("repoRoot is required")
        }
        if (visitor == null) {
            throw new IllegalArgumentException("visitor is required")
        }
        // traverse the object tree

        return repoRoot
    }
}
