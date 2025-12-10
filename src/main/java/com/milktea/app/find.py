import os

def read_files_in_directory(directory_path, output_file):
    """
    读取指定目录及其子目录中的所有文件内容，并保存到单个文本文件中。

    Args:
        directory_path (str): 要扫描的根目录路径。
        output_file (str): 保存所有文件内容的输出文本文件路径。
    """
    with open(output_file, 'w', encoding='utf-8') as outfile:
        for root, dirs, files in os.walk(directory_path):
            for file in files:
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as infile:
                        content = infile.read()
                        outfile.write(f"--- 文件名: {file_path} ---\n")
                        outfile.write(content)
                        outfile.write("\n\n") # 添加空行以分隔文件内容
                except Exception as e:
                    print(f"读取文件 {file_path} 时出错: {e}")

if __name__ == "__main__":
    # 获取当前脚本文件所在的目录
    script_directory = os.path.dirname(os.path.abspath(__file__))
    target_directory = script_directory # 将扫描目标设置为脚本所在目录

    # 替换为你想要保存的输出文件名
    output_text_file = 'all_files_content_from_script_dir.txt'

    if os.path.isdir(target_directory):
        read_files_in_directory(target_directory, output_text_file)
        print(f"所有文件内容已成功保存到: {output_text_file}")
    else:
        print(f"错误: 目录 '{target_directory}' 不存在。这不应该发生，因为它是脚本所在的目录。")