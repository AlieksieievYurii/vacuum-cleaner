# TODO creating python env
# TODO installing requirements
# TODO copy project $ run python
from argparse import ArgumentParser, Namespace
from pathlib import Path
from typing import List

import paramiko


class RemotePythonException(Exception):
    pass


class RemotePython(object):
    def __init__(self, server: str, user: str, password: str):
        self.server = server
        self.user = user
        self.password = password

    def create_python_environment(self, path: Path, env_name: str) -> None:
        a = self._execute_command(['python3', '-m', 'venv', path.joinpath(env_name).as_posix()])
        print(a[0], a[1])

    def _execute_command(self, command: List[str]):
        with paramiko.SSHClient() as ssh_client:
            ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            ssh_client.connect(self.server, username=self.user, password=self.password)
            _, stdout, stderr = ssh_client.exec_command(' '.join(command))
            return stdout.readlines(), stderr.readlines()


def create_python_env(remote_python: RemotePython, args: Namespace) -> None:
    remote_python.create_python_environment(
        path=args.env_folder,
        env_name=args.name
    )


def main(arguments: Namespace):
    remote_python = RemotePython(server=arguments.server, user=arguments.user, password=arguments.password)
    actions = {
        'env': create_python_env
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

    try:
        main(argument_parser.parse_args())
    except RemotePythonException as error:
        print(error)
        exit(1)
