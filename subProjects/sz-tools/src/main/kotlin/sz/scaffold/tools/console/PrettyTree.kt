package sz.scaffold.tools.console

//
// Created by kk on 2019-05-18.
//

@Suppress("MemberVisibilityCanBePrivate")
object HierarchyTxt {
    val empty = "    "
    val starte = "├── "
    val middle = "│   "
    val end = "└── "

    fun isHierarchyText(text: String): Boolean {
        return setOf(empty, starte, middle, end).contains(text)
    }

    fun isEmpty(text: String): Boolean {
        return text == empty
    }

    fun isNodeText(text: String): Boolean {
        return isHierarchyText(text).not()
    }

    fun rightSideIsNodeText(rowIndex: Int, columnIndex: Int, grid: MutableList<MutableList<String>>): Boolean {
        val row = grid[rowIndex]
        val rightSideColumn = columnIndex + 1
        if (rightSideColumn > row.lastIndex) {
            return false
        }
        return isNodeText(row[rightSideColumn])
    }

    fun rightSideIsHierarchyText(rowIndex: Int, columnIndex: Int, grid: MutableList<MutableList<String>>): Boolean {
        val row = grid[rowIndex]
        val rightSideColumn = columnIndex + 1
        if (rightSideColumn > row.lastIndex) {
            return false
        }
        return isHierarchyText(row[rightSideColumn])
    }

    fun drawHierarchy(grid: MutableList<MutableList<String>>) {
        (grid.lastIndex downTo 0).forEach { rowIndex ->
            val row = grid[rowIndex]
            val columnIndex = row.indexOfFirst { isNodeText(it) } - 1
            if (columnIndex >= 0 && isEmpty(row[columnIndex])) {
                row[columnIndex] = end
                var tmpRowIdx = rowIndex - 1
                while (tmpRowIdx >= 0) {
                    val tmpRow = grid[tmpRowIdx]
                    if (isNodeText(tmpRow[columnIndex])) {
                        break
                    }
                    if (rightSideIsNodeText(tmpRowIdx, columnIndex, grid)) {
                        tmpRow[columnIndex] = starte
                    }
                    if (rightSideIsHierarchyText(tmpRowIdx, columnIndex, grid)) {
                        tmpRow[columnIndex] = middle
                    }

                    tmpRowIdx--
                }
            }
        }
    }
}

class TreeNode(val text: String) {
    val children = mutableListOf<TreeNode>()

    fun unfold(currentLevel: Int, grid: MutableList<MutableList<String>>) {
        val result = mutableListOf<String>()
        (0 until currentLevel).forEach {
            result.add(HierarchyTxt.empty)
        }
        result.add(this.text)
        grid.add(result)

        children.forEach { child ->
            child.unfold(currentLevel + 1, grid)
        }
    }


}

class PrettyTree(val root: TreeNode) {

    fun prettyTxt(): String {
        val grid = mutableListOf<MutableList<String>>()
        root.unfold(0, grid)
        HierarchyTxt.drawHierarchy(grid)
        return grid.joinToString("\n") { row ->
            row.joinToString("")
        }
    }
}

