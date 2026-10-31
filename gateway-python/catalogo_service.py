import requests
from typing import List, Dict

# Caché simple en memoria para evitar saturar la API externa
CACHE_MARCAS: List[str] = []
CACHE_MODELOS: Dict[str, List[str]] = {}

# Marcas populares predeterminadas por si la API externa no responde (Fallback)
MARCAS_POPULARES = [
    "Toyota", "Volkswagen", "Ford", "Chevrolet", "Fiat", 
    "Peugeot", "Renault", "Honda", "Nissan", "Hyundai", "Audi", "BMW"
]

def obtener_marcas_externas() -> List[str]:
    global CACHE_MARCAS
    if CACHE_MARCAS:
        return CACHE_MARCAS
    
    url = "https://vpic.nhtsa.dot.gov/api/vehicles/GetMakesForVehicleType/car?format=json"
    try:
        res = requests.get(url, timeout=5)
        if res.status_code == 200:
            data = res.json().get("Results", [])
            # Extraemos y ordenamos nombres únicos en formato Capitalize
            marcas = sorted(list({item["MakeName"].strip().title() for item in data if item.get("MakeName")}))
            
            # Priorizamos las marcas más comunes en Argentina al principio
            principales = [m for m in MARCAS_POPULARES if m in marcas]
            resto = [m for m in marcas if m not in MARCAS_POPULARES]
            CACHE_MARCAS = principales + resto
            return CACHE_MARCAS
    except Exception:
        pass
    
    # Fallback si falla internet o la API externa
    return sorted(MARCAS_POPULARES)

def obtener_modelos_por_marca_externa(marca: str) -> List[str]:
    marca_key = marca.strip().lower()
    if marca_key in CACHE_MODELOS:
        return CACHE_MODELOS[marca_key]

    url = f"https://vpic.nhtsa.dot.gov/api/vehicles/getmodelsformake/{marca_key}?format=json"
    try:
        res = requests.get(url, timeout=5)
        if res.status_code == 200:
            data = res.json().get("Results", [])
            modelos = sorted(list({item["Model_Name"].strip().title() for item in data if item.get("Model_Name")}))
            CACHE_MODELOS[marca_key] = modelos
            return modelos
    except Exception:
        pass

    return []