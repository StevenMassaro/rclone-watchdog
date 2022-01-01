export function handleRestResponse(res) {
    if (res.ok) {
        return res.json();
    } else {
        throw res;
    }
}