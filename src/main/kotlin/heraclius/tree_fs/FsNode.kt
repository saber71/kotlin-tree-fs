package heraclius.tree_fs

import heraclius.tools.DateUtils
import heraclius.tools.Tree
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.DosFileAttributes
import java.nio.file.attribute.PosixFileAttributes

class FsNode(val file: File, private val name: String? = null, private val parent: FsNode? = null) : Tree.Node {
    private val children =
        lazy {
            (if (file.isDirectory) file.listFiles()?.toList() ?: mutableListOf()
            else mutableListOf()).map { FsNode(it, parent = this) }
        }
    private var attributeList: List<Tree.NodeAttribute>? = null

    override fun getChildren(): List<Tree.LeafNode> {
        return children.value
    }

    override val isLeaf: Boolean
        get() = children.value.isEmpty()

    override fun getName(): String {
        return name ?: file.name
    }

    override fun getAttributes(): List<Tree.NodeAttribute> {
        if (attributeList == null) {
            val attributes = Files.readAttributes(Paths.get(file.path), BasicFileAttributes::class.java)
            var fileType = FileType.REGULAR
            if (attributes.isDirectory) fileType = FileType.DIRECTORY
            else if (attributes.isSymbolicLink) fileType = FileType.SYMBOLIC_LINK
            else if (attributes.isOther) fileType = FileType.OTHER
            val list = mutableListOf(
                Tree.NodeAttribute(FileAttribute.ATIME.name, DateUtils.from(attributes.lastModifiedTime().toInstant())),
                Tree.NodeAttribute(FileAttribute.MTIME.name, DateUtils.from(attributes.lastModifiedTime().toInstant())),
                Tree.NodeAttribute(
                    FileAttribute.CREATE_TIME.name,
                    DateUtils.from(attributes.lastModifiedTime().toInstant())
                ),
                Tree.NodeAttribute(FileAttribute.TYPE.name, fileType),
                Tree.NodeAttribute(FileAttribute.SIZE.name, attributes.size()),
            )
            if (attributes is DosFileAttributes) {
                list.add(Tree.NodeAttribute(FileAttribute.ARCHIVE.name, attributes.isArchive))
                list.add(Tree.NodeAttribute(FileAttribute.HIDDEN.name, attributes.isHidden))
                list.add(Tree.NodeAttribute(FileAttribute.READONLY.name, attributes.isReadOnly))
                list.add(Tree.NodeAttribute(FileAttribute.SYSTEM.name, attributes.isSystem))
            } else if (attributes is PosixFileAttributes) {
                list.add(Tree.NodeAttribute(FileAttribute.GROUP.name, attributes.group().name))
                list.add(Tree.NodeAttribute(FileAttribute.OWNER.name, attributes.owner().name))
                list.add(Tree.NodeAttribute(FileAttribute.PERMISSIONS.name, attributes.permissions().toList()))
            }
            attributeList = list
        }
        return attributeList!!
    }

    override fun getParent(): Tree.LeafNode? {
        return parent
    }
}
