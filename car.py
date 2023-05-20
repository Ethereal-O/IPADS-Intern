from controller import controller
from info.info_manager import info_manager_thread_local, infoManager


def start(id):
    info_manager_thread_local.info_manager = infoManager()
    info_manager_thread_local.info_manager.set_id(id)
    controller.start()
