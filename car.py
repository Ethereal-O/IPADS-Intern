from controller import controller
from info.info_manager import info_manager

def start(id):
    info_manager.set_id(id)
    controller.start()
