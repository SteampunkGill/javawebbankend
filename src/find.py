import os

def summarize_python_code(file_path, max_lines=5):
    """
    尝试为 Python 文件生成一个简单的内容概括。
    主要关注文件开头的注释和函数定义。

    Args:
        file_path (str): Python 文件的路径。
        max_lines (int): 用于概括的最大行数。

    Returns:
        str: 文件的概括内容，如果无法概括则返回空字符串。
    """
    summary = []
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            lines_read = 0
            for line in f:
                stripped_line = line.strip()
                if not stripped_line: # 跳过空行
                    continue
                # 优先考虑文档字符串（docstring）
                if stripped_line.startswith('"""') or stripped_line.startswith("'''"):
                    summary.append(stripped_line)
                    # 尝试读取整个 docstring
                    while True:
                        line = f.readline()
                        if not line:
                            break
                        summary.append(line.strip())
                        if line.strip().endswith('"""') or line.strip().endswith("'''"):
                            break
                    break # 找到docstring后，可以停止读取，除非max_lines很大
                # 查找函数定义
                elif stripped_line.startswith('def '):
                    summary.append(stripped_line)
                    break # 找到第一个函数定义，可以停止读取
                # 查找类定义
                elif stripped_line.startswith('class '):
                    summary.append(stripped_line)
                    break # 找到第一个类定义，可以停止读取
                # 查找注释
                elif stripped_line.startswith('#'):
                    summary.append(stripped_line)

                lines_read += 1
                if lines_read >= max_lines and summary: # 如果已经读了足够多的行并且有内容了，就停止
                    break
            return "\n".join(summary)
    except Exception as e:
        # print(f"Error summarizing {file_path}: {e}") # 可选：打印错误信息
        return "" # 发生错误时返回空字符串

def find_and_save_files(directory, output_txt="found_files.txt"):
    """
    查找指定目录及其所有子目录下的所有文件，并将结果保存到文本文件。
    对 Python 文件尝试进行内容概括。

    Args:
        directory (str): 要开始查找的目录路径。
        output_txt (str): 保存结果的文本文件名。
    """
    found_items = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_path = os.path.join(root, file)
            relative_path = os.path.relpath(file_path, directory) # 获取相对路径，更简洁

            item_info = f"File: {relative_path}"
            if file.lower().endswith(".py"):
                summary = summarize_python_code(file_path)
                if summary:
                    item_info += f"\n  Summary: {summary}"
            found_items.append(item_info)

    try:
        with open(output_txt, 'w', encoding='utf-8') as f:
            if found_items:
                for item in found_items:
                    f.write(item + "\n" + "-"*20 + "\n") # 用分隔线分隔每个条目
                print(f"已将找到的文件列表和概括保存到 '{output_txt}'。")
            else:
                f.write("未找到任何文件。\n")
                print("未找到任何文件。")
    except IOError as e:
        print(f"写入文件 '{output_txt}' 时出错: {e}")


if __name__ == "__main__":
    # 获取当前脚本所在的目录
    current_directory = os.path.dirname(os.path.abspath(__file__))

    print(f"正在查找目录 '{current_directory}' 及其子目录下的所有文件，并将结果保存到 'found_files.txt'...")
    find_and_save_files(current_directory, "found_files.txt")