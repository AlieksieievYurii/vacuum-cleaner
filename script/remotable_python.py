from argparse import ArgumentParser, Namespace
from contextlib import contextmanager
from pathlib import Path
from typing import List, Optional

import paramiko
from paramiko import SFTPClient


class RemotePythonException(Exception):
    pass


class SSHClient(object):
    def __init__(self, server: str, username: str, password: str):
        self.server = server
        self.username = username
        self.password = password

    def connect(self):
        ssh_client = paramiko.SSHClient()
        ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh_client.connect(hostname=self.server, username=self.username, password=self.password)
        setattr(self, 'ssh_client', ssh_client)
        return self

    @property
    def _ssh_client(self):
        return getattr(self, 'ssh_client')

    def close(self):
        self._ssh_client.close()

    def execute(self, command: List[str], print_continuously: bool = False):
        _, stdout, stderr = self._ssh_client.exec_command(' '.join(command), get_pty=print_continuously)

        for line in iter(stdout.readline, ""):
            print(line, end='')

        print(''.join(stderr.readlines()))

    def copy_folder(self, local_folder: Path, remote_destination: Path, sftp: Optional[SFTPClient] = None) -> None:
        """
            Copy the whole local folder to remote server. For example: (local) C:/users/newgo/FolderToCopy will be
            copied to /home/user/FolderToCopy. FolderToCopy will be created on the remote server. It the folder
            already exists, the exception will be raised.

        :param local_folder: Path to a local folder to copy
        :param remote_destination: Path to a destination folder
        :param sftp: sftp client instance
        :return: None
        """
        sftp = sftp if sftp else self._ssh_client.open_sftp()
        sftp.mkdir(remote_destination.joinpath(local_folder.name).as_posix())
        for l_dir in local_folder.iterdir():
            if l_dir.is_file():
                sftp.put(l_dir.as_posix(), remote_destination.joinpath(local_folder.name, l_dir.name).as_posix())
            elif l_dir.is_dir():
                self.copy_folder(l_dir, remote_destination.joinpath(local_folder.name), sftp)


class RemotePython(object):
    def __init__(self, server: str, user: str, password: str):
        self.server = server
        self.user = user
        self.password = password

    @contextmanager
    def open_ssh_client(self):
        ssh_client = SSHClient(server=self.server, username=self.user, password=self.password)
        try:
            yield ssh_client.connect()
        finally:
            ssh_client.close()

    def create_python_environment(self, path: Path, env_name: str) -> None:
        with self.open_ssh_client() as ssh_client:
            ssh_client.execute(['python3', '-m', 'venv', path.joinpath(env_name).as_posix()])

    def execute_python_project(self, project: Path, file: Path, remote_destination: Path) -> None:
        remote_project_folder = remote_destination / project.name
        with self.open_ssh_client() as ssh_client:
            ssh_client.execute([f'rm -rf {remote_project_folder.as_posix()}'])
            ssh_client.copy_folder(project, remote_destination)
            ssh_client.execute(['python3', f'{remote_project_folder.joinpath(file).as_posix()}'],
                               print_continuously=True)


def create_python_env(remote_python: RemotePython, args: Namespace) -> None:
    remote_python.create_python_environment(
        path=args.env_folder,
        env_name=args.name
    )


def execute_python_project(remote_python: RemotePython, args: Namespace) -> None:
    remote_python.execute_python_project(
        project=args.project,
        file=args.execute_file,
        remote_destination=args.remote_destination
    )


def main(arguments: Namespace):
    remote_python = RemotePython(server=arguments.server, user=arguments.user, password=arguments.password)
    actions = {
        'env': create_python_env,
        'execute-project': execute_python_project
    }

    actions[arguments.action](remote_python, arguments)


if __name__ == '__main__':
    argument_parser = ArgumentParser(description='Allows manage Python remotely')
    argument_parser.add_argument('--server', required=True)
    argument_parser.add_argument('--user', required=True)
    argument_parser.add_argument('--password', required=True)
    sub_parser = argument_parser.add_subparsers(dest='action', required=True)

    python_env_action = sub_parser.add_parser('env', help='Creates python virtual env remotely')
    python_env_action.add_argument('--env-folder', type=Path, required=True, help='Path where env will be created')
    python_env_action.add_argument('--name', type=str, required=True, help='Env folder name')

    python_execute_project = sub_parser.add_parser('execute-project', help='Copy given project and execute')
    python_execute_project.add_argument('--remote-destination', type=Path, required=True)
    python_execute_project.add_argument('--project', type=Path, required=True)
    python_execute_project.add_argument('--execute-file', type=Path, required=True)

    try:
        main(argument_parser.parse_args())
    except RemotePythonException as error:
        print(error)
        exit(1)
